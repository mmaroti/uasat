/**
 * Copyright (C) Miklos Maroti, 2015-2016
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.uasat.math;

import java.io.File;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import org.uasat.core.*;

public class XmlWriter {
	private static DocumentBuilder BUILDER;
	private static TransformerFactory FACTORY;

	private final Document document;
	private final Transformer transformer;

	public XmlWriter() {
		try {
			if (BUILDER == null)
				BUILDER = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
			if (FACTORY == null)
				FACTORY = TransformerFactory.newInstance();

			document = BUILDER.newDocument();
			transformer = FACTORY.newTransformer();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Element createOperationNode(Operation<Boolean> operation, String name) {
		Element op = document.createElement("op");

		Element opSymbol = document.createElement("opSymbol");
		op.appendChild(opSymbol);

		Element opName = document.createElement("opName");
		opName.setTextContent(name);
		opSymbol.appendChild(opName);

		Element arity = document.createElement("arity");
		arity.setTextContent(Integer.toString(operation.getArity()));
		opSymbol.appendChild(arity);

		Element opTable = document.createElement("opTable");
		op.appendChild(opTable);

		Element intArray = document.createElement("intArray");
		opTable.appendChild(intArray);

		Tensor<Integer> tensor = Operation.decode(operation);
		int size = operation.getSize();

		if (tensor.getOrder() == 0) {
			Element row = document.createElement("row");
			row.setTextContent(Integer.toString(tensor.get()));
			intArray.appendChild(row);
		} else if (tensor.getOrder() == 1) {
			String s = "";
			for (int i = 0; i < size; i++) {
				if (i > 0)
					s += ',';
				s += tensor.getElem(i);
			}
			Element row = document.createElement("row");
			row.setTextContent(s);
			intArray.appendChild(row);
		} else {
			int[] v = new int[tensor.getOrder()];
			outer: for (;;) {
				String s = "";
				for (int i = 0; i < size; i++) {
					if (i > 0)
						s += ',';
					v[v.length - 1] = i;
					s += tensor.getElem(v);
				}

				String r = "[";
				for (int i = 0; i < v.length - 1; i++) {
					if (i > 0)
						r += ',';
					r += v[i];
				}
				r += ']';

				Element row = document.createElement("row");
				row.setAttribute("r", r);
				row.setTextContent(s);
				intArray.appendChild(row);

				for (int i = v.length - 2; i >= 0; i--) {
					if (++v[i] < size)
						continue outer;
					else
						v[i] = 0;
				}
				break;
			}
		}

		return op;
	}

	public Element createAlgebraNode(Algebra<Boolean> algebra, String name,
			String[] opNames) {
		if (algebra.getOperations().size() != opNames.length)
			throw new IllegalArgumentException();

		Element alg = document.createElement("algebra");

		Element basicAlg = document.createElement("basicAlgebra");
		alg.appendChild(basicAlg);

		Element algName = document.createElement("algName");
		algName.setTextContent(name);
		basicAlg.appendChild(algName);

		Element card = document.createElement("cardinality");
		card.setTextContent(Integer.toString(algebra.getSize()));
		basicAlg.appendChild(card);

		Element ops = document.createElement("operations");
		for (int i = 0; i < opNames.length; i++) {
			ops.appendChild(createOperationNode(algebra.getOperation(i),
					opNames[i]));
		}
		basicAlg.appendChild(ops);

		return alg;
	}

	public void writeToFile(String filename) {
		document.setXmlStandalone(true);
		DOMSource source = new DOMSource(document);
		StreamResult target = new StreamResult(new File(filename));
		try {
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(source, target);
		} catch (TransformerException e) {
			throw new RuntimeException(e);
		}
	}

	public static void writeAlgebra(Algebra<Boolean> algebra, String name,
			String[] opNames, String filename) {
		XmlWriter writer = new XmlWriter();
		Element alg = writer.createAlgebraNode(algebra, name, opNames);
		writer.document.appendChild(alg);
		writer.writeToFile(filename);
	}
}
