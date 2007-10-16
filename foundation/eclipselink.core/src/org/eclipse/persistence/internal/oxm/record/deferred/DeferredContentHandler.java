/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/ 
package org.eclipse.persistence.internal.oxm.record.deferred;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.persistence.oxm.record.UnmarshalRecord;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * <p><b>Purpose</b>: ContentHandler to store events until we know if we are dealing with a simple, complex or empty element.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Store events until will know if the element is simple, complex or empty
 * <li> Return control to the original unmarshalRecord
 * </ul>
 */
public abstract class DeferredContentHandler implements ContentHandler, LexicalHandler {
    private int levelIndex;
    private List<SAXEvent> events;
    private UnmarshalRecord parent;
    private boolean startOccurred;
    private boolean charactersOccurred;

    public DeferredContentHandler(UnmarshalRecord parentRecord) {
        levelIndex = 0;
        events = new ArrayList<SAXEvent>();
        this.parent = parentRecord;
    }

    protected abstract void processEmptyElement() throws SAXException;

    protected abstract void processComplexElement() throws SAXException;

    protected abstract void processSimpleElement() throws SAXException;

    protected void executeEvents(UnmarshalRecord unmarshalRecord) throws SAXException {
        for (int i = 0; i < events.size(); i++) {
            SAXEvent nextEvent = events.get(i);
            nextEvent.processEvent(unmarshalRecord);
        }
        if (parent.getXMLReader().getContentHandler().equals(this)) {
            parent.getXMLReader().setContentHandler(unmarshalRecord);
        }
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        StartPrefixMappingEvent event = new StartPrefixMappingEvent(prefix, uri);
        events.add(event);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        EndPrefixMappingEvent event = new EndPrefixMappingEvent(prefix);
        events.add(event);
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        levelIndex++;
        StartElementEvent event = new StartElementEvent(uri, localName, qName, atts);
        events.add(event);        
        
        if (startOccurred) {
            //we know it's complex and non-null
            processComplexElement();
            return;
        }
        
        startOccurred = true;
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        levelIndex--;
        EndElementEvent event = new EndElementEvent(uri, localName, qName);
        events.add(event);

        if (charactersOccurred) {
            //we know it is a simple element
            processSimpleElement();
        } else if(startOccurred){
            //we know it is an empty element            
            processEmptyElement();
        }

        if ((levelIndex == 0) && (parent != null)) {
            parent.getXMLReader().setContentHandler(parent);
        }
    }

    public void setDocumentLocator(Locator locator) {
        DocumentLocatorEvent event = new DocumentLocatorEvent(locator);
        events.add(event);
    }

    public void startDocument() throws SAXException {
        StartDocumentEvent event = new StartDocumentEvent();
        events.add(event);
    }

    public void endDocument() throws SAXException {
        EndDocumentEvent event = new EndDocumentEvent();
        events.add(event);
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        charactersOccurred = true;
        CharactersEvent event = new CharactersEvent(ch, start, length);
        events.add(event);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        IgnorableWhitespaceEvent event = new IgnorableWhitespaceEvent(ch, start, length);
        events.add(event);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        ProcessingInstructionEvent event = new ProcessingInstructionEvent(target, data);
        events.add(event);
    }

    public void skippedEntity(String name) throws SAXException {
        SkippedEntityEvent event = new SkippedEntityEvent(name);
        events.add(event);
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        StartDTDEvent event = new StartDTDEvent(name, publicId, systemId);
        events.add(event);
    }

    public void endDTD() throws SAXException {
        EndDTDEvent event = new EndDTDEvent();
        events.add(event);
    }

    public void startEntity(String name) throws SAXException {
        StartEntityEvent event = new StartEntityEvent(name);
        events.add(event);
    }

    public void endEntity(String name) throws SAXException {
        EndEntityEvent event = new EndEntityEvent(name);
        events.add(event);
    }

    public void startCDATA() throws SAXException {
        StartCDATAEvent event = new StartCDATAEvent();
        events.add(event);
    }

    public void endCDATA() throws SAXException {
        EndCDATAEvent event = new EndCDATAEvent();
        events.add(event);
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        CommentEvent event = new CommentEvent(ch, start, length);
        events.add(event);
    }

    protected UnmarshalRecord getParent() {
        return parent;
    }

    protected List getEvents() {
        return events;
    }
}