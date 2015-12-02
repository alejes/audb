package net.sf.jsqlparser.expression;


/**
 * A terminal expression that can not be evaluated further (e.g., a Number or String)
 */

public interface PrimitiveValue {

    long toLong() throws InvalidPrimitive;

    double toDouble() throws InvalidPrimitive;

    class InvalidPrimitive extends Exception {
    }

}