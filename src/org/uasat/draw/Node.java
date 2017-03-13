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

import java.awt.Point;

public class Node {
	private final Point center;
	private final String label;

	private int state = 0;
	public final static int SELECTED = 0x01;
	public final static int FIXED_XCOORD = 0x02;
	public final static int FIXED_YCOORD = 0x04;
	public final static int FIXED_COORDS = FIXED_XCOORD | FIXED_YCOORD;

	private double forcex;
	private double forcey;

	public Node() {
		this.center = new Point();
		this.label = null;
	}

	public Node(String label) {
		this.center = new Point();
		this.label = label;
	}

	public Node(Point center) {
		this.center = center;
		this.label = null;
	}

	public Node(Point center, String label) {
		this.center = center;
		this.label = label;
	}

	public void resetForces() {
		forcex = 0.0;
		forcey = 0.0;
	}

	public void updateForces(double x, double y) {
		forcex += x;
		forcey += y;
	}

	public void applyForces(double speed) {
		center.x += forcex * speed;
		center.y += forcey * speed;
	}

	public Point getCenter() {
		return center;
	}

	public String getLabel() {
		return label;
	}

	public void move(Point offset) {
		center.x += offset.x;
		center.y += offset.y;
	}

	public boolean isSelected() {
		return (state & SELECTED) != 0;
	}

	public void setSelected(boolean selected) {
		if (selected)
			state |= SELECTED;
		else
			state &= ~SELECTED;
	}
}
