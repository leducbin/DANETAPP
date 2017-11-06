package com.movideo.baracus.xml;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by rranawaka on 24/11/2015.
 */
public class XmlParser
{
	SAXParserFactory saxParserFactory;

	public XmlParser()
	{
		saxParserFactory = SAXParserFactory.newInstance();
	}

	public <T> T parse(InputStream inputStream, SAXHandler<T> saxHandler) throws IOException, SAXException, ParserConfigurationException
	{
		try
		{
			SAXParser saxParser = saxParserFactory.newSAXParser();
			saxParser.parse(inputStream, saxHandler);

			return saxHandler.getResult();
		} catch (ParserConfigurationException e)
		{
			throw e;
		} catch (SAXException e)
		{
			throw e;
		} catch (IOException e)
		{
			throw e;
		}
	}
}
