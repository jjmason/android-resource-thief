package jm.rt.res;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import brut.androlib.*;
import brut.androlib.res.*;
import brut.androlib.res.data.*;
import brut.androlib.res.decoder.*;
import brut.androlib.res.decoder.ARSCDecoder.ARSCData;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.util.Xml;

public class APKFile extends ZipFile { 
	public static final String RESOURCE_TABLE_PATH = "resources.arsc";
			
	public APKFile(PackageInfo packageInfo) throws IOException { 
		this(packageInfo.applicationInfo);
	}
	
	public APKFile(ApplicationInfo applicationInfo) throws IOException {
		super(applicationInfo.sourceDir);
	}
	
	public InputStream open(String path) throws IOException {
		ZipEntry entry = findEntry(path);
		if(entry == null){
			throw new FileNotFoundException(path);
		}
		return getInputStream(entry);
	}
	
	public XmlPullParser openXml(String path) throws XmlPullParserException, IOException {
		InputStream in = open(path);
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(in, "UTF-8");
		return parser;
	}
	
	public ResPackage[] getResources() throws IOException, ResException{
		InputStream in = open(RESOURCE_TABLE_PATH);
		ARSCData data = ARSCDecoder.decode(in, false, false);
		return data.getPackages();
	}
	
	public String[] list(String path){
		ArrayList<String> result = new ArrayList<String>();
		for(Enumeration<? extends ZipEntry> entries = entries();
				entries.hasMoreElements();){
			ZipEntry entry = entries.nextElement();
			if(entry.getName().startsWith(path)){
				String name = entry.getName();
				name = name.substring(path.length());
				name = name.split("\\/")[0];
				result.add(name);
			}
		}
		return result.toArray(new String[result.size()]);
	}
	
	private ZipEntry findEntry(String path){
		for(Enumeration<? extends ZipEntry> entries = entries();
				entries.hasMoreElements();){
			ZipEntry entry = entries.nextElement();
			if(entry.getName().equalsIgnoreCase(path)){
				return entry;
			}
		}
		return null;
	}
}
