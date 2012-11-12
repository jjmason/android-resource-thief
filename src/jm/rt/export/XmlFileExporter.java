package jm.rt.export;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import brut.androlib.res.decoder.AXmlResourceParser;
import brut.androlib.res.decoder.ResAttrDecoder;

import android.util.Xml;

public class XmlFileExporter {
    public static final String FEATURE_INDENT_OUTPUT = 
    		"http://xmlpull.org/v1/doc/features.html#indent-output";
    
	
    
	public static void export(XmlPullParser p, XmlSerializer s)
			throws XmlPullParserException, IOException{ 
		int e;
		while((e = p.next()) != XmlPullParser.END_DOCUMENT){
			switch(e){
			case XmlPullParser.TEXT:
				s.text(p.getText());
				break;
			case XmlPullParser.START_TAG:
				s.startTag(p.getNamespace(), p.getName());
				for(int i=0;i<p.getAttributeCount();i++){
					s.attribute(p.getAttributeNamespace(i), p.getAttributeName(i), p.getAttributeValue(i));
				}
				break;
			case XmlPullParser.END_TAG:
				s.endTag(p.getNamespace(), p.getName());
				break;
			}
		}
	}
}
