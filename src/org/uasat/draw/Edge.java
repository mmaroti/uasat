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

public class Edge {
	private final Node node1;
	private final Node node2;

	private int state = 0;
	private final static int STATE_DISPLAY_ARROW = 0x01;
	private final static int STATE_VERT_ORDERED = 0x02;

	public Edge(Node node1, Node node2) {
		this.node1 = node1;
		this.node2 = node2;
	}

	public Edge(Node node1, Node node2, boolean oriented) {
		this.node1 = node1;
		this.node2 = node2;
	}

	public boolean isDisplayArrow() {
		return (state & STATE_DISPLAY_ARROW) != 0;
	}

	public void setDisplayArrow(boolean enable) {
		if (enable)
			state |= STATE_DISPLAY_ARROW;
		else
			state &= ~STATE_DISPLAY_ARROW;
	}

	public boolean isVertOrdered() {
		return (state & STATE_VERT_ORDERED) != 0;
	}

	public boolean isSelected() {
		return node1.isSelected() || node2.isSelected();
	}

	public void draw(Graphics2D graphics) {
		graphics.setColor(Color.BLACK);

		Point p1 = node1.getCenter();
		Point p2 = node2.getCenter();

		if (p1.distance(p2) > node1.getRadius() + node2.getRadius() + 4) {
			double a = Math.atan2(p2.y - p1.y, p2.x - p1.x);

			int p1x = p1.x + (int) (Math.cos(a) * (node1.getRadius() + 2));
			int p1y = p1.y + (int) (Math.sin(a) * (node1.getRadius() + 2));
			int p2x = p2.x - (int) (Math.cos(a) * (node2.getRadius() + 2));
			int p2y = p2.y - (int) (Math.sin(a) * (node2.getRadius() + 2));

			graphics.drawLine(p1x, p1y, p2x, p2y);

			if (isDisplayArrow()) {
				final double b = 0.3;
				final int r = (node2.getRadius() + 2) * 2;

				int p3x = p2x - (int) (Math.cos(a + b) * r);
				int p3y = p2y - (int) (Math.sin(a + b) * r);
				int p4x = p2x - (int) (Math.cos(a - b) * r);
				int p4y = p2y - (int) (Math.sin(a - b) * r);

				graphics.fillPolygon(new int[] { p2x, p3x, p4x }, new int[] { p2y, p3y, p4y }, 3);
			}
		}
	}

	public void updateForces() {
		double d = node1.getCenter().distance(node2.getCenter());
		
	}
}
