package jm.rt.export;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import jm.rt.util.XmlPullParserWrapper;
import jm.util.Args;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import brut.androlib.res.data.ResPackage;
import brut.androlib.res.data.ResResSpec;
import brut.androlib.res.data.ResResource;
import brut.androlib.res.data.ResType;
import brut.androlib.res.data.ResValuesFile;
import brut.androlib.res.data.value.ResFileValue;
import brut.androlib.res.decoder.AXmlResourceParser;
import brut.androlib.res.decoder.Res9patchStreamDecoder;
import brut.androlib.res.decoder.ResAttrDecoder;
import brut.androlib.res.xml.ResValuesXmlSerializable;
import brut.androlib.util.IOUtils;

public class Exporter {
	private static final String TAG = "Exporter";
	private Context mContext;
	private ApkFile mApkFile;
	private String mExportPath;
	private Set<Integer> mResIds = new LinkedHashSet<Integer>();
	private Collection<String> mAssets = new LinkedHashSet<String>();
	private Set<Integer> mExportIds = new LinkedHashSet<Integer>();
	private Set<Integer> mReferencedIds = new LinkedHashSet<Integer>();

	private static class ResFileInfo {
		public ResResource resource;
		public ResFileValue file;

		public ResFileInfo(ResResource r) {
			resource = r;
			file = (ResFileValue) r.getValue();
		}
	}

	private Set<ResFileInfo> mFiles = new LinkedHashSet<ResFileInfo>();
	private Set<ResValuesFile> mValues = new LinkedHashSet<ResValuesFile>();
 

	public Exporter(Context context, ApkFile apkFile, String exportPath,
			Collection<Integer> resIds, Collection<String> assets) {
		mContext = Args.notNull(context);
		mApkFile = Args.notNull(apkFile);
		mExportPath = Args.notNull(exportPath);
		if(resIds != null)
			mResIds.addAll(resIds);
		if(assets != null)
			mAssets.addAll(assets);
	}

	public void export() throws Exception {
		dbg("initial res count = %d", mResIds.size());
		Set<Integer> allResIds = collectReferences(mResIds);
		dbg("after resolving refs count = %d", allResIds.size());

		for (ResPackage pkg : mApkFile.getResTable().listMainPackages()) {
			for (ResResource rr : pkg.listFiles(allResIds)) {
				mFiles.add(new ResFileInfo(rr));
			}
			mValues.addAll(pkg.listValuesFiles(allResIds));
		}

		dbg("exporting %d files and %d values files", mFiles.size(),
				mValues.size());

		for (ResFileInfo rfi : mFiles) {
			exportFile(rfi);
		}

		for (ResValuesFile rvf : mValues)  {
			exportValuesFile(rvf);
		}

		dbg("DONE!");
	}

	private void exportFile(ResFileInfo fileInfo) {
		try {
			exportFileX(fileInfo);
		} catch (Exception e) {
			err("error exporting file " + fileInfo.file.getPath(), e);
		}
	}

	private void exportValuesFile(ResValuesFile valuesFile) {
		try {
			exportValuesFileX(valuesFile);
		} catch (Exception e) {
			err("error exporting values file " + valuesFile.getPath(), e);
		}
	}

	private void exportFileX(ResFileInfo fileInfo) throws Exception {
		String path = fileInfo.file.getPath();
		if (path.endsWith(".xml")) {
			exportXmlFile(fileInfo);
		} else if(path.endsWith(".9.png")){
			export9PatchFile(fileInfo);
		} else {
			exportRawFile(fileInfo);
		}
	}

	private void export9PatchFile(ResFileInfo fileInfo) throws Exception{
		InputStream in = openInput(fileInfo.file.getPath());
		OutputStream out = openOutput(fileInfo.file.getPath());
		try{
			new Res9patchStreamDecoder().decode(in, out);
		}finally{
			out.close();
			in.close();
		}
	}
	
	private void exportXmlFile(ResFileInfo fileInfo) throws Exception {
		InputStream input = openInput(fileInfo.file.getPath());
		OutputStream output = openOutput(fileInfo.file.getPath());
		try {
			ResAttrDecoder attrs = new ResAttrDecoder();
			attrs.setCurrentPackage(fileInfo.resource.getResSpec().getPackage());
			AXmlResourceParser parser = new AXmlResourceParser(input);
			parser.setAttrDecoder(attrs);
			XmlSerializer serializer = getXmlSerializer();
			serializer.setOutput(output, "utf-8");
			exportXmlFile(parser, serializer);
		} finally {
			output.close();
			input.close();
		}
	}

	/**
	 * @return
	 */
	private XmlSerializer getXmlSerializer() {
		XmlSerializer serializer = Xml.newSerializer();
		serializer.setFeature(
				"http://xmlpull.org/v1/doc/features.html#indent-output",
				true);
		return serializer;
	}

	private void exportXmlFile(XmlPullParser p, XmlSerializer s) 
		throws Exception{
		
		int e;
		while ((e = p.next()) != XmlPullParser.END_DOCUMENT) {
			switch (e) {
			case XmlPullParser.TEXT:
				s.text(p.getText());
				break;
			case XmlPullParser.START_TAG:
				s.startTag(p.getNamespace(), p.getName());
				for (int i = 0; i < p.getAttributeCount(); i++) {
					s.attribute(p.getAttributeNamespace(i),
							p.getAttributeName(i), p.getAttributeValue(i));
				}
				break;
			case XmlPullParser.END_TAG:
				s.endTag(p.getNamespace(), p.getName());
				break;
			}
		}
	}

	private void exportRawFile(ResFileInfo fileInfo) throws IOException {
		InputStream in = openInput(fileInfo.file.getPath());
		OutputStream out = openOutput(fileInfo.file.getPath());
		// TODO rename files
		try {
			IOUtils.copy(in, out);
			dbg("exported file " + fileInfo.file.getPath());
			// TODO report progress
		} finally {
			out.close();
			in.close();
		}
	}

	AXmlResourceParser parser = new AXmlResourceParser();

	private InputStream openInput(String path) throws IOException {
		return mApkFile.open(path);
	}

	private static String trimPath(String path){
		if(path.charAt(0) == '/'){
			path = path.substring(1);
		}
		if(path.charAt(path.length() - 1) == '/'){
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}
	
	private OutputStream openOutput(String path) throws IOException {
		path = trimPath(path);
		File exportDir = new File(mExportPath);
		File exportFile = new File(exportDir, path);
		File parent = exportFile.getParentFile();
		if(!parent.exists()){
			if(!parent.mkdirs()){
				throw new IOException("error creating directory "  + parent.getPath());
			}
		}
		if(!parent.isDirectory()){
			throw new IOException("parent file " + parent.getPath() + " is not a directory");
		}

		// TODO make this behavior configurable
		if (exportFile.exists()) {
			warn("overwriting file " + exportFile.getAbsolutePath());
		}
		
		dbg("opening file %s (export dir is %s)", exportFile.getPath(), exportDir.getPath());
		
		return new BufferedOutputStream(new FileOutputStream(exportFile));
	}

	private void exportValuesFileX(ResValuesFile valuesFile) throws Exception {
		OutputStream out = openOutput(valuesFile.getPath());
		dbg("exporting values to %s...", valuesFile.getPath());
		try{
		
			exportValuesFileX(out, valuesFile);
		}finally{
			out.close();
		}
		dbg("done exporting values");		
	}
	
	private void exportValuesFileX(OutputStream out, ResValuesFile values)
		throws Exception {
		XmlSerializer s = getXmlSerializer();
		s.setOutput(out, "utf-8");
		s.startDocument(null,null);
		s.startTag(null, "resources");
		for(ResResource rr:values.listResources()){
			if(values.isSynthesized(rr))
				continue;
            ((ResValuesXmlSerializable) rr.getValue()).serializeToResValuesXml(s, rr);
		}
		s.endTag(null, "resources");
		s.endDocument();
	}

	private Set<Integer> collectReferences(Collection<Integer> ids) {
		Set<Integer> seenIds = new LinkedHashSet<Integer>();
		Set<Integer> newIds = new LinkedHashSet<Integer>();
		newIds.addAll(ids);
		while (!newIds.isEmpty()) {
			Set<ResResource> newFiles = collectXmlFiles(newIds);
			seenIds.addAll(newIds);
			newIds.clear();
			for (ResResource file : newFiles) {
				for (int id : collectReferencesFromXml(((ResFileValue) file
						.getValue()).getPath())) {
					if (!seenIds.contains(id)) {
						newIds.add(id);
					}
				}
			}
		}
		return seenIds;
	}

	private Set<Integer> collectReferencesFromXml(String path) {
		Set<Integer> result = new LinkedHashSet<Integer>();
		if (path == null || !path.endsWith(".xml")) {
			return result;
		}
		try {
			parseXmlForReferences(path, result);
		} catch (Exception e) {
			err("parsing " + path, e);
		}
		return result;
	}

	private void parseXmlForReferences(String path, Set<Integer> outResult)
			throws Exception {
		InputStream in = mApkFile.open(path);
		try {
			AXmlResourceParser p = new AXmlResourceParser(in);
			parseXmlForReferences(p, outResult);
		} finally {
			in.close();
		}
	}

	private void parseXmlForReferences(XmlPullParser parser,
			Set<Integer> outResult) throws Exception {
		XmlPullParserWrapper p = new XmlPullParserWrapper(parser);
		while (p.nextStartTag()) {
			AttributeSet attrs = Xml.asAttributeSet(p);
			for (int i = 0; i < attrs.getAttributeCount(); ++i) {
				String value = attrs.getAttributeValue(i);
				if (value.charAt(0) == '@') {
					int id = getIdFromFullName(value.substring(1));
					if (id != 0) {
						outResult.add(id);
					}
				}
			}
		}
	}

	private int getIdFromFullName(String fullName) {
		String[] sp = fullName.split(":");
		String pkgName = null, rest = null;
		if (sp.length == 1) {
			pkgName = null;
			rest = sp[0];
		} else if (sp.length == 2) {
			pkgName = sp[0];
			rest = sp[1];
		} else {
			dbg("wtf res named %s???", fullName);
			return 0;
		}

		sp = rest.split("/");
		String type = null, name = null;
		if (sp.length == 2) {
			type = sp[0];
			name = sp[1];
		} else {
			dbg("wtf res named %s (%s)???", fullName, rest);
			return 0;
		}

		if(pkgName!=null&&pkgName.equals("android")){
			ResPackage p = mApkFile.getResTable().listFramePackages().iterator().next();
			ResType t = p.getType(type);
			if(t != null){
				ResResSpec s = t.getResSpec(name);
				if(s != null){
					return s.getId().id;
				}
			}
		}
		for (ResPackage pkg : mApkFile.getResTable().listMainPackages()) {

			if (pkgName == null || pkgName.equals(pkg.getName())) {
				ResType t = pkg.getType(type);
				if (t != null) {
					ResResSpec s = t.getResSpec(name);
					if (s != null) {
						return s.getId().id;
					}
				}
			}
		}
		return 0;
	}

	private Set<ResResource> collectXmlFiles(Collection<Integer> ids) {
		Set<ResResource> result = new HashSet<ResResource>();
		for (ResResource rr : collectFiles(ids)) {
			ResFileValue f = (ResFileValue) rr.getValue();
			if (f.getPath().endsWith(".xml")) {
				result.add(rr);
			}
		}
		return result;
	}

	private Set<ResResource> collectFiles(Collection<Integer> ids) {
		Set<ResResource> result = new HashSet<ResResource>();
		for (ResPackage rp : mApkFile.getResTable().listMainPackages()) {
			result.addAll(rp.listFiles(ids));
		}
		return result;
	}

	private static void dbg(String message, Object... args) {
		message = String.format(message, args);
		Log.d(TAG, message);
	}

	private static void warn(String message) {
		Log.w(TAG, message);
	}

	private static void err(String message) {
		err(message, null);
	}

	private static void err(String message, Exception ex) {
		err(message, ex, false);
	}

	private static void err(String message, Exception ex, boolean fatal) {
		if (ex != null) {
			Log.e(TAG, message, ex);
		} else {
			Log.e(TAG, message);
		}
		if (fatal) {
			throw new RuntimeException(ex);
		}
	}
}
