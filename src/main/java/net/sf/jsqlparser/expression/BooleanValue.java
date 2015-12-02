
/* ================================================================
 * JSQLParser : java based sql parser 
 * ================================================================
 *
 * Project Info:  http://jsqlparser.sourceforge.net
 * Project Lead:  Leonardo Francalanci (leoonardoo@yahoo.it);
 *
 * (C) Copyright 2004, by Leonardo Francalanci
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package net.sf.jsqlparser.expression;

/**
 * Every number without a point or an exponential format is a LongValue
 */
public class BooleanValue implements PrimitiveValue, Expression {
    public static final BooleanValue
            TRUE = new BooleanValue(true),
            FALSE = new BooleanValue(false);
    private boolean value;

    private BooleanValue(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    public long toLong() {
        return value ? 1 : 0;
    }

    public double toDouble() {
        return value ? 1.0 : 0.0;
    }

    public String toString() {
        return value ? "TRUE" : "FALSE";
    }

    public boolean equals(Object o) {
        try {
            return value == (((BooleanValue) o).value);
        } catch (ClassCastException e) {
            return false;
        }
    }

    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }
}