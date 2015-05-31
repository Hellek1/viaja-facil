package eu.hellek.createstops.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import eu.hellek.createstops.data.StringStack;

/*
 * Source: http://www.kuxas.com/68/xml-parsing-and-display-of-gpx-data-in-java.html
 */
public class GpxParser extends org.xml.sax.helpers.DefaultHandler {
	   private StringStack elementNames;
	   private StringBuilder contentBuffer;
	   private int totalPoints;
	   private String line;
	   private String type;
	   private FileWriter file;
	   private String text;
	   private int lasttype; // 1 = trk, 2 = trkpt

	   public GpxParser(String line, String type) {
	      clear();
	      this.line = line;
	      this.type = type;
	      lasttype = 0;
	   }
	   
	   public void clear() {
		  totalPoints = 0;
	      elementNames = new StringStack();
	      contentBuffer = new StringBuilder();
	   }
	   
	   /*
	    * READ GPX DATA FILE
	    */
	   public int read(String filename) {
	      clear();

	      try {
	         FileInputStream in = new FileInputStream(new File(filename));
	         InputSource source = new InputSource(in);
	         
	         XMLReader parser = org.xml.sax.helpers.XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
	         parser.setContentHandler(this);
	         parser.parse(source);
	         in.close();
	      } catch (FileNotFoundException e) {
	         e.printStackTrace();
	      } catch (UnsupportedEncodingException e) {
	         e.printStackTrace();
	      } catch (SAXException e) {
	         e.printStackTrace();
	      } catch (IOException e) {
	         e.printStackTrace();
	      }
	      return 0;
	   }
	   
	   /*
	    *  DefaultHandler::startElement() fires whenever an XML start tag is encountered 
	    * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	    */
	   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {	   
		  if (localName.compareToIgnoreCase("trkpt") == 0) {
			  totalPoints++;
			  lasttype = 2;
			  text += attributes.getValue("lat") + "," + attributes.getValue("lon") + ",";
	      }
		  if (localName.compareToIgnoreCase("trk") == 0) {
			  lasttype = 1;
	      }
		   
	      // Clear content buffer
	      contentBuffer.delete(0, contentBuffer.length());
	      
	      // Store name of current element in stack
	      elementNames.push(qName);
	   }
	   
	   /* 
	    * the DefaultHandler::characters() function fires 1 or more times for each text node encountered
	    * 
	    */
	   public void characters(char[] ch, int start, int length) throws SAXException {
	      contentBuffer.append(String.copyValueOf(ch, start, length));
	   }
	   
	   /* 
	    * the DefaultHandler::endElement() function fires for each end tag
	    * 
	    */
	   public void endElement(String uri, String localName, String qName) throws SAXException {
	      String currentElement = elementNames.pop();
	      if(currentElement != null) {
			  if(lasttype == 1 && currentElement.compareToIgnoreCase("name") == 0) {
				  try {
					String fileNamePart = contentBuffer.toString().replace('/', ' ');
					file = new FileWriter("out/" + type + "/" + line + "-" + fileNamePart + ".txt");
				} catch (IOException e) {
					e.printStackTrace();
				}
				  text = line+","+contentBuffer.toString()+","+type+",";
			  }
			  if(currentElement.compareToIgnoreCase("trk") == 0) {
				  try {
					text = text.substring(0, text.length()-1);
					file.write(text);
					file.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			  }
			  if(lasttype == 2 && currentElement.compareToIgnoreCase("name") == 0) {
				text += contentBuffer.toString() + ",";
				if(!contentBuffer.toString().contains(",") && (type.equals("subte") || type.equals("tren"))) {
					text += contentBuffer.toString() + ",";
				}
			  }
	      }
	   }

	public int getTotalPoints() {
		return totalPoints;
	}
	   
}