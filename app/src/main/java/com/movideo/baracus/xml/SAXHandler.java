/*******************************************************************************
 * Copyright © Movideo Pty Limited 2013. All Rights Reserved
 ******************************************************************************/
package com.movideo.baracus.xml;

import com.movideo.baracus.util.ICallback;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Generic SAX Xml parser
 *
 * @param <T> Type of Object generated from this parser
 * @author rranawaka
 */
public abstract class SAXHandler<T> extends DefaultHandler
{

	protected XMLReader xmlReader;
	protected T result;
	private ICallback<T> endSectionCallback;

	private String rootElementName;

	public SAXHandler()
	{
		super();
	}

	public SAXHandler(XMLReader xmlReader, ICallback<T> endSectionCallback, String rootElementName)
	{
		super();
		this.xmlReader = xmlReader;
		this.endSectionCallback = endSectionCallback;
		this.rootElementName = rootElementName;
	}

	/**
	 * Receive notification of the start of an element.
	 */
	@Override
	public final void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		startElementFound(uri, localName, qName, attributes);
	}

	/**
	 * Receive notification of the end of an element.
	 */
	@Override
	public final void endElement(String uri, String localName, String qName) throws SAXException
	{
		endElementFound(uri, localName, qName);

		if (rootElementName != null && rootElementName.equals(qName) && endSectionCallback != null)
		{
			endSectionCallback.onSuccess(result);
		}
	}

	/**
	 * Receive notification of the start of an element.
	 *
	 * @param uri
	 * @param localName
	 * @param qName
	 * @param attributes
	 * @throws SAXException
	 */
	protected abstract void startElementFound(String uri, String localName, String qName, Attributes attributes) throws SAXException;

	/**
	 * Receive notification of the end of an element.
	 *
	 * @param uri
	 * @param localName
	 * @param qName
	 * @throws SAXException
	 */
	protected abstract void endElementFound(String uri, String localName, String qName) throws SAXException;

	/**
	 * @return the generated object
	 */
	public T getResult()
	{
		return result;
	}

	/**
	 * sets SAX Reader
	 *
	 * @param xmlReader
	 */
	public void setXmlReader(XMLReader xmlReader)
	{
		this.xmlReader = xmlReader;
	}
}
