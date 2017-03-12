/**
 * Copyright (C) Miklos Maroti, 2016
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

package org.uasat.draw;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Graph {
	private final List<Node> nodes = new ArrayList<Node>();
	private final List<Edge> edges = new ArrayList<Edge>();

	public void draw(Graphics2D g) {
		for (Edge e : edges)
			e.draw(g);

		for (Node n : nodes)
			n.draw(g);
	}

	public void applyForces(double speed) {
		for (Node n : nodes)
			n.resetForces();

		for (Edge e : edges)
			e.updateForces();

		for (Node n : nodes)
			n.applyForces(speed);
	}

	public void clear() {
		nodes.clear();
		edges.clear();
	}

	public void add(Node node) {
		nodes.add(node);
	}

	public void add(Edge edge) {
		edges.add(edge);
	}

	public void unselectAll() {
		for (Node n : nodes)
			n.setSelected(false);
	}

	public Node find(Point point) {
		for (Node n : nodes)
			if (n.contains(point))
				return n;
		return null;
	}

	public void select(Rectangle rect) {
		for (Node n : nodes)
			n.setSelected(rect.contains(n.getCenter()));
	}

	public void toggle(Point point) {
		for (Node n : nodes) {
			if (n.contains(point)) {
				n.setSelected(!n.isSelected());
				return;
			}
		}
	}

	public List<Node> getSelected() {
		List<Node> list = new ArrayList<Node>();
		for (Node n : nodes)
			if (n.isSelected())
				list.add(n);

		return list;
	}

	public boolean hasSelection() {
		for (Node n : nodes)
			if (n.isSelected())
				return true;

		return false;
	}

	public void moveSlected(Point offset) {
		for (Node n : nodes)
			if (n.isSelected())
				n.move(offset);
	}

	public void removeSelected() {
		ListIterator<Edge> i1 = edges.listIterator();
		while (i1.hasNext()) {
			Edge e = i1.next();
			if (e.isSelected())
				i1.remove();
		}

		ListIterator<Node> i2 = nodes.listIterator();
		while (i2.hasNext()) {
			Node n = i2.next();
			if (n.isSelected())
				i2.remove();
		}
	}

	public static Graph createN5() {
		Graph graph = new Graph();

		Node n0 = new Node("0");
		Node na = new Node("a");
		Node nb = new Node("b");
		Node nc = new Node("c");
		Node n1 = new Node("1");

		graph.add(n0);
		graph.add(na);
		graph.add(nb);
		graph.add(nc);
		graph.add(n1);

		graph.add(new Edge(n0, na, true));
		graph.add(new Edge(n0, nb, true));
		graph.add(new Edge(na, n1, true));
		graph.add(new Edge(nb, nc, true));
		graph.add(new Edge(nc, n1, true));

		return graph;
	}

	public static Graph createGrid(int size) {
		Graph graph = new Graph();

		for (int x = 0; x < size; x += 1)
			for (int y = 0; y < size; y += 1)
				graph.add(new Node(new Point(x * 10, y * 10)));

		return graph;
	}
}
