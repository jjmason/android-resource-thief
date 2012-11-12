package jm.rt.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class XmlPullParserWrapper implements XmlPullParser{
	private final XmlPullParser p;

	public XmlPullParserWrapper(XmlPullParser p){
		this.p = p;
	}
	
	public void moveToFirstTag() throws XmlPullParserException,IOException { 
		if(!nextStartTag())
			throw new XmlPullParserException("missing root tag");
	}
	
	public boolean nextStartTag() throws XmlPullParserException, IOException {
		int e;
		while((e = next()) != START_TAG &&
				e != END_DOCUMENT){/*empty*/}
		return e != END_DOCUMENT;
	}
	
	public void setFeature(String name, boolean state)
			throws XmlPullParserException {
		p.setFeature(name, state);
	}

	public boolean getFeature(String name) {
		return p.getFeature(name);
	}

	public void setProperty(String name, Object value)
			throws XmlPullParserException {
		p.setProperty(name, value);
	}

	public Object getProperty(String name) {
		return p.getProperty(name);
	}

	public void setInput(Reader in) throws XmlPullParserException {
		p.setInput(in);
	}

	public void setInput(InputStream inputStream, String inputEncoding)
			throws XmlPullParserException {
		p.setInput(inputStream, inputEncoding);
	}

	public String getInputEncoding() {
		return p.getInputEncoding();
	}

	public void defineEntityReplacementText(String entityName,
			String replacementText) throws XmlPullParserException {
		p.defineEntityReplacementText(entityName, replacementText);
	}

	public int getNamespaceCount(int depth) throws XmlPullParserException {
		return p.getNamespaceCount(depth);
	}

	public String getNamespacePrefix(int pos) throws XmlPullParserException {
		return p.getNamespacePrefix(pos);
	}

	public String getNamespaceUri(int pos) throws XmlPullParserException {
		return p.getNamespaceUri(pos);
	}

	public String getNamespace(String prefix) {
		return p.getNamespace(prefix);
	}

	public int getDepth() {
		return p.getDepth();
	}

	public String getPositionDescription() {
		return p.getPositionDescription();
	}

	public int getLineNumber() {
		return p.getLineNumber();
	}

	public int getColumnNumber() {
		return p.getColumnNumber();
	}

	public boolean isWhitespace() throws XmlPullParserException {
		return p.isWhitespace();
	}

	public String getText() {
		return p.getText();
	}

	public char[] getTextCharacters(int[] holderForStartAndLength) {
		return p.getTextCharacters(holderForStartAndLength);
	}

	public String getNamespace() {
		return p.getNamespace();
	}

	public String getName() {
		return p.getName();
	}

	public String getPrefix() {
		return p.getPrefix();
	}

	public boolean isEmptyElementTag() throws XmlPullParserException {
		return p.isEmptyElementTag();
	}

	public int getAttributeCount() {
		return p.getAttributeCount();
	}

	public String getAttributeNamespace(int index) {
		return p.getAttributeNamespace(index);
	}

	public String getAttributeName(int index) {
		return p.getAttributeName(index);
	}

	public String getAttributePrefix(int index) {
		return p.getAttributePrefix(index);
	}

	public String getAttributeType(int index) {
		return p.getAttributeType(index);
	}

	public boolean isAttributeDefault(int index) {
		return p.isAttributeDefault(index);
	}

	public String getAttributeValue(int index) {
		return p.getAttributeValue(index);
	}

	public String getAttributeValue(String namespace, String name) {
		return p.getAttributeValue(namespace, name);
	}

	public int getEventType() throws XmlPullParserException {
		return p.getEventType();
	}

	public int next() throws XmlPullParserException, IOException {
		return p.next();
	}

	public int nextToken() throws XmlPullParserException, IOException {
		return p.nextToken();
	}

	public void require(int type, String namespace, String name)
			throws XmlPullParserException, IOException {
		p.require(type, namespace, name);
	}

	public String nextText() throws XmlPullParserException, IOException {
		return p.nextText();
	}

	public int nextTag() throws XmlPullParserException, IOException {
		return p.nextTag();
	} 
	
}
