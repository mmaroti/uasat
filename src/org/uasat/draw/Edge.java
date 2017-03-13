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

public class Edge {
	private final Node node1;
	private final Node node2;

	private int state;
	public final static int DISPLAY_ARROW = 0x01;
	public final static int VERT_ORDERED = 0x02;

	public Edge(Node node1, Node node2) {
		this.node1 = node1;
		this.node2 = node2;
		this.state = 0;
	}

	public Edge(Node node1, Node node2, int state) {
		this.node1 = node1;
		this.node2 = node2;
		this.state = state;
	}

	public Node getNode1() {
		return node1;
	}

	public Node getNode2() {
		return node2;
	}

	public boolean isDisplayArrow() {
		return (state & DISPLAY_ARROW) != 0;
	}

	public void setDisplayArrow(boolean enable) {
		if (enable)
			state |= DISPLAY_ARROW;
		else
			state &= ~DISPLAY_ARROW;
	}

	public boolean isVertOrdered() {
		return (state & VERT_ORDERED) != 0;
	}

	public boolean isSelected() {
		return node1.isSelected() || node2.isSelected();
	}

	public void updateForces() {
		double d = node1.getCenter().distance(node2.getCenter());

	}
}
