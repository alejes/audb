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

import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;

/**
 * It represents a "-" before an expression
 */
public class InverseExpression implements Expression {
    private Expression expression;

    public InverseExpression() {
    }

    public InverseExpression(Expression expression) {
        setExpression(expression);
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public String toString() {
        if (
                (expression instanceof BooleanValue)
                        || (expression instanceof AndExpression)
                        || (expression instanceof OrExpression)
                        || (expression instanceof Between)
                        || (expression instanceof EqualsTo)
                        || (expression instanceof ExistsExpression)
                        || (expression instanceof GreaterThan)
                        || (expression instanceof GreaterThanEquals)
                        || (expression instanceof InExpression)
                        || (expression instanceof IsNullExpression)
                        || (expression instanceof LikeExpression)
                        || (expression instanceof Matches)
                        || (expression instanceof MinorThan)
                        || (expression instanceof MinorThanEquals)
                        || (expression instanceof NotEqualsTo)
                ) {
            return "NOT (" + expression.toString() + ")";
        } else {
            return "-(" + expression.toString() + ")";
        }
    }
}
