/*--

 Copyright 2000 Brett McLaughlin & Jason Hunter. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
    this list of conditions, and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions, the disclaimer that follows these conditions,
    and/or other materials provided with the distribution.

 3. The names "JDOM" and "Java Document Object Model" must not be used to
    endorse or promote products derived from this software without prior
    written permission. For written permission, please contact
    license@jdom.org.

 4. Products derived from this software may not be called "JDOM", nor may
    "JDOM" appear in their name, without prior written permission from the
    JDOM Project Management (pm@jdom.org).

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 JDOM PROJECT  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Java Document Object Model Project and was originally
 created by Brett McLaughlin <brett@jdom.org> and
 Jason Hunter <jhunter@jdom.org>. For more  information on the JDOM
 Project, please see <http://www.jdom.org/>.

 */
package org.jdom.output;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Entity;
import org.jdom.Namespace;
import org.jdom.NoSuchElementException;
import org.jdom.ProcessingInstruction;

/**
 * <p><code>XMLOutputter</code> takes a JDOM tree and
 *   formats it to a stream as XML.  This formatter performs typical
 *   document formatting.  The XML declaration
 *   and processing instructions are always on their own lines.  Empty
 *   elements are printed as <empty/> and text-only contents are printed as
 *   <tag>content</tag> on a single line.  Constructor parameters control the
 *   indent amount and whether new lines are printed between elements.  For
 *   compact machine-readable output pass in an empty string indent and a
 *   <code>false</code> for printing new lines.
 * </p>
 *
 * @author Brett McLaughlin
 * @author Jason Hunter
 * @author Jason Reid
 * @author Wolfgang Werner
 * @version 1.0
 */
public class XMLOutputter {

    /** The default indent, two spaces */
    private String indent = "  ";

    /** The default new line flag, set to do new lines */
    private boolean newlines = true;

    /** The encoding format */
    private String encoding = "UTF8";

    /** Namespaces on the document */
    private LinkedList namespaces;

    /**
     * <p>
     * This will create an <code>XMLOutputter</code> with
     *   a two-space indent and new lines on.
     * </p>
     */
    public XMLOutputter() {
        namespaces = new LinkedList();
    }

    /**
     * <p>
     * This will create an <code>XMLOutputter</code> with
     *   the given indent and new lines on.
     * </p>
     *
     * @param indent  the indent string, usually some number of spaces
     */
    public XMLOutputter(String indent) {
       this.indent = indent;
       namespaces = new LinkedList();
    }

    /**
     * <p>
     * This will create an <code>XMLOutputter</code> with
     *   the given indent and new lines printing only if newlines is
     *   <code>true</code>.
     * </p>
     *
     * @param indent the indent <code>String</code>, usually some number
     *        of spaces
     * @param newlines <code>true</code> indicates new lines should be
     *                 printed, else new lines are ignored (compacted).
     */
    public XMLOutputter(String indent, boolean newlines) {
       this.indent = indent;
       this.newlines = newlines;
       namespaces = new LinkedList();
    }

    /**
     * <p>
     * This will create an <code>XMLOutputter</code> with
     *   the given indent and new lines printing only if newlines is
     *   <code>true</code>, and encoding format <code>encoding</code>.
     * </p>
     *
     * @param indent the indent <code>String</code>, usually some number
     *        of spaces
     * @param newlines <code>true</code> indicates new lines should be
     *                 printed, else new lines are ignored (compacted).
     * @param encoding set encoding format.
     */
    public XMLOutputter(String indent, boolean newlines, String encoding) {
       this.indent = indent;
       this.newlines = newlines;
       this.encoding = encoding;
       namespaces = new LinkedList();
    }

    /**
     * <p>
     * This will print the proper indent characters for the given indent level.
     * </p>
     *
     * @param out <code>PrintWriter</code> to write to
     * @param level <code>int</code> indentation level
     */
    protected void indent(PrintWriter out, int level) {
        for (int i = 0; i < level; i++) {
            out.print(indent);
        }
    }

    /**
     * <p>
     * This will print a new line only if the newlines flag was set to true
     * </p>
     *
     * @param out <code>PrintWriter</code> to write to
     */
    protected void maybePrintln(PrintWriter out) {
        if (newlines) out.println();
    }

    /**
     * <p>
     * This will print the <code>Document</code> to the given output stream.
     *   The characters are printed using UTF-8 encoding.
     * </p>
     *
     * @param doc <code>Document</code> to format.
     * @param out <code>PrintWriter</code> to write to.
     * @param encoding set encoding format.
     * @throws <code>IOException</code> - if there's any problem writing.
     */
    public void output(Document doc, OutputStream out, String encoding)
                                           throws IOException {
        this.encoding = encoding;
        output(doc, out);
    }

    /**
     * <p>
     * This will print the <code>Document</code> to the given output stream.
     *   The characters are printed using UTF-8 encoding.
     * </p>
     *
     * @param doc <code>Document</code> to format.
     * @param out <code>PrintWriter</code> to write to.
     * @throws <code>IOException</code> - if there's any problem writing.
     */
    public void output(Document doc, OutputStream out)
                                           throws IOException {
        /**
         * Get a PrintWriter, use general-purpose UTF-8 encoding.
         *   Specify it as "UTF8" to work with older JDK versions.
         */
        PrintWriter writer = new PrintWriter(
                             new OutputStreamWriter(
                             new BufferedOutputStream(out), encoding));

        // Print out XML declaration
        printDeclaration(doc, writer);

        printDocType(doc.getDocType(), writer);

        // Print out processing instructions
        printProcessingInstructions(doc.getProcessingInstructions(), writer);

        // Print out elements, starting with no indention
        try {
            Iterator i = doc.getMixedContent().iterator();
            while (i.hasNext()) {
                Object obj = i.next();
                if (obj instanceof Element) {
                    // 0 is indentation
                    printElement(doc.getRootElement(), writer, 0);
                } else if (obj instanceof Comment) {
                    writer.print("<!--" + obj.toString() + "-->");
                }
            }
        } catch (NoSuchElementException e) {
            // No elements to print
        }

        // Flush the output
        writer.flush();
    }

    /**
     * <p>
     * This will print the declaration to the given output stream.
     *   Assumes XML version 1.0 since we don't directly know.
     * </p>
     *
     * @param docType <code>DocType</code> whose declaration to write.
     * @param out <code>PrintWriter</code> to write to.
     */
    protected void printDeclaration(Document doc, PrintWriter out) {
        // Assume 1.0 version
        if (encoding.equals("UTF8"))
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        else
            out.println("<?xml version=\"1.0\" encoding=\"" + encoding +
                        "\"?>");
    }

    /**
     * <p>
     * This will print the DOCTYPE declaration if one exists.
     * </p>
     *
     * @param doc <code>Document</code> whose declaration to write.
     * @param out <code>PrintWriter</code> to write to.
     */
    protected void printDocType(DocType docType, PrintWriter out) {
        if (docType == null) {
            return;
        }

        String publicID = docType.getPublicID();
        String systemID = docType.getSystemID();
        boolean hasPublic = false;

        out.print("<!DOCTYPE ");
        out.print(docType.getElementName());
        if ((publicID != null) && (!publicID.equals(""))) {
            out.print(" PUBLIC \"");
            out.print(publicID);
            out.print("\"");
            hasPublic = true;
        }
        if ((systemID != null) && (!systemID.equals(""))) {
            if (!hasPublic) {
                out.print(" SYSTEM");
            }
            out.print(" \"");
            out.print(systemID);
            out.print("\"");
        }
        out.print(">");
        out.println();
        out.println();
    }

    /**
     * <p>
     * This will print the processing instructions to the given output stream.
     *   Assumes the <code>List</code> contains nothing but
     *   <code>ProcessingInstruction</code> objects.
     * </p>
     *
     * @param pis <code>List</code> of ProcessingInstructions to print
     * @param out <code>PrintWriter</code> to write to
     */
    protected void printProcessingInstructions(List pis, PrintWriter out) {
        for (int i=0, size=pis.size(); i<size; i++) {
            ProcessingInstruction pi =
                (ProcessingInstruction)pis.get(i);
            out.print("<?");
            out.print(pi.getTarget());
            out.print(" ");
            out.print(pi.getData());
            out.print("?>");
            out.println();
        }
        out.println();
    }

    /**
     * <p>
     * This will handle printing out an <code>{@link Element}</code>,
     *   its <code>{@link Attribute}</code>s, and its value.
     * </p>
     *
     * @param element <code>Element</code> to output.
     * @param out <code>PrintWriter</code> to write to.
     * @param indent <code>int</code> level of indention.
     */
    protected void printElement(Element element, PrintWriter out,
                                int indentLevel) {

        List mixedContent = element.getMixedContent();

        boolean empty = mixedContent.size() == 0;
        boolean stringOnly =
            !empty &&
            mixedContent.size() == 1 &&
            mixedContent.get(0) instanceof String;

        // Print beginning element tag
        maybePrintln(out);
        indent(out, indentLevel);

        // Print the beginning of the tag plus attributes and any
        // necessary namespace declarations
        out.print("<");
        out.print(element.getQualifiedName());
        Namespace ns = element.getNamespace();
        boolean printedNS = false;
        if ((ns != Namespace.NO_NAMESPACE) && (!namespaces.contains(ns))) {
            printNamespace(element.getNamespace(), out);
            printedNS = true;
            namespaces.add(ns);
        }

        printAttributes(element.getAttributes(), out);

        if (empty) {
            // Simply close up
            out.print(" />");
        } else if (stringOnly) {
            // Print the tag  with String on same line
            // Example: <tag name="value">content</tag>
            out.print(">");
            out.print(escapeElementEntities(element.getContent()));
            out.print("</");
            out.print(element.getQualifiedName());
            out.print(">");
        } else {
            /**
             * Print with children on future lines
             * Rather than check for mixed content or not, just print
             * Example: <tag name="value">
             *             <child/>
             *          </tag>
             */
            out.print(">");

            // Iterate through children
            Object content = null;
            for (int i=0, size=mixedContent.size(); i<size; i++) {
                content = mixedContent.get(i);
                // See if text or an element
                if (content instanceof String) {
                    /*
                     * XXX: We handle the 5 XML 1.0 entities
                     *   but what about CDATA? (brett)
                     */
                    out.print(escapeElementEntities(content.toString()));
                } else if (content instanceof Comment) {
                    maybePrintln(out);
                    indent(out, indentLevel + 1);  // one extra
                    out.print(((Comment)content).getSerializedForm());
                } else if (content instanceof Element) {
                    printElement((Element)content, out, indentLevel + 1);
                    //maybePrintln(out);
                } else if (content instanceof Entity) {
                    out.print("&");
                    out.print(((Entity)content).getSerializedForm());
                    out.print(";");
                } else if (content instanceof ProcessingInstruction) {
                    maybePrintln(out);
                    indent(out, indentLevel + 1);
                    ProcessingInstruction pi =
                        (ProcessingInstruction)content;
                    out.print("<?");
                    out.print(pi.getTarget());
                    out.print(" ");
                    out.print(pi.getData());
                    out.print("?>");
                } // Unsupported types are *not* printed
            }

            maybePrintln(out);
            indent(out, indentLevel);
            out.print("</");
            out.print(element.getQualifiedName());
            out.print(">");

           // After recursion, remove the namespace defined on the element (if any)
          if (printedNS) {
              namespaces.removeLast();
           }
        }
    }

    /**
     * <p>
     *  This will handle printing out any needed <code>{@link Namespace}</code>
     *    declarations.
     * </p>
     *
     * @param ns <code>Namespace</code> to print definition of
     * @param out <code>PrintWriter</code> to write to.
     */
    protected void printNamespace(Namespace ns, PrintWriter out) {
        out.print(" xmlns");
        if (!ns.getPrefix().equals("")) {
            out.print(":");
            out.print(ns.getPrefix());
        }
        out.print("=\"");
        out.print(ns.getURI());
        out.print("\"");
    }

    /**
     * <p>
     * This will handle printing out an <code>{@link Attribute}</code> list.
     * </p>
     *
     * @param attributes <code>List</code> of Attribute objcts
     * @param out <code>PrintWriter</code> to write to
     */
    protected void printAttributes(List attributes, PrintWriter out) {
        for (int i=0, size=attributes.size(); i<size; i++) {
            Attribute attribute = (Attribute)attributes.get(i);
            Namespace ns = attribute.getNamespace();
            if ((ns != Namespace.NO_NAMESPACE) && (!namespaces.contains(ns))) {
                printNamespace(attribute.getNamespace(), out);
            }
            out.print(" ");
            out.print(attribute.getQualifiedName());
            out.print("=");

            out.print("\"");
            out.print(escapeAttributeEntities(attribute.getValue()));
            out.print("\"");
        }
    }

    /**
     * <p>
     * This will take the five pre-defined entities in XML 1.0 and
     *   convert their character representation to the appropriate
     *   entity reference, suitable for XML attributes.
     * </p>
     *
     * @param st <code>String</code> input to escape.
     * @return <code>String</code> with escaped content.
     */
    private String escapeAttributeEntities(String st) {
        StringBuffer buff = new StringBuffer();
        char[] block = st.toCharArray();
        String stEntity = null;
        int i, last;

        for (i=0, last=0; i < block.length; i++) {
            switch(block[i]) {
                case '<' :
                    stEntity = "&lt;";
                    break;
                case '>' :
                    stEntity = "&gt;";
                    break;
                case '\'' :
                    stEntity = "&apos;";
                    break;
                case '\"' :
                    stEntity = "&quot;";
                    break;
                case '&' :
                    stEntity = "&amp;";
                    break;
                default :
                    /* no-op */ ;
            }
            if (stEntity != null) {
                buff.append(block, last, i - last);
                buff.append(stEntity);
                stEntity = null;
                last = i + 1;
            }
        }
        if(last < block.length) {
            buff.append(block, last, i - last);
        }

        return buff.toString();
    }


    /**
     * <p>
     * This will take the three pre-defined entities in XML 1.0
     *   (used specifically in XML elements) and
     *   convert their character representation to the appropriate
     *   entity reference, suitable for XML element.
     * </p>
     *
     * @param st <code>String</code> input to escape.
     * @return <code>String</code> with escaped content.
     */
    private String escapeElementEntities(String st) {
        StringBuffer buff = new StringBuffer();
        char[] block = st.toCharArray();
        String stEntity = null;
        int i, last;

        for (i=0, last=0; i < block.length; i++) {
            switch(block[i]) {
                case '<' :
                    stEntity = "&lt;";
                    break;
                case '>' :
                    stEntity = "&gt;";
                    break;
                case '&' :
                    stEntity = "&amp;";
                    break;
                default :
                    /* no-op */ ;
            }
            if (stEntity != null) {
                buff.append(block, last, i - last);
                buff.append(stEntity);
                stEntity = null;
                last = i + 1;
            }
        }
        if(last < block.length) {
            buff.append(block, last, i - last);
        }

        return buff.toString();
    }

}