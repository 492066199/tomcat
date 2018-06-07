package com.sailing.tomcat.io;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;


/**
 * Wrapper around the standard <code>java.io.PrintWriter</code> that keeps
 * track of whether or not any characters have ever been written (even if they
 * are still buffered inside the PrintWriter or any other Writer that it uses
 * above the underlying TCP/IP socket).  This is required by the semantics of
 * several calls on ServletResponse, which are required to throw an
 * <code>IllegalStateException</code> if output has ever been written.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2004/08/26 21:30:19 $
 * @deprecated
 */

public class ResponseWriter extends PrintWriter {


    public ResponseWriter(OutputStreamWriter writer) {
        super(writer);
    }

    public void print(boolean b) {
        super.print(b);
        super.flush();
    }

    public void print(char c) {
        super.print(c);
        super.flush();
    }

    public void print(char ca[]) {
        super.print(ca);
        super.flush();
    }

    public void print(double d) {
        super.print(d);
        super.flush();
    }

    public void print(float f) {
        super.print(f);
        super.flush();
    }

    public void print(int i) {
        super.print(i);
        super.flush();
    }

    public void print(long l) {
        super.print(l);
        super.flush();
    }

    public void print(Object o) {
        super.print(o);
        super.flush();
    }

    public void print(String s) {
        super.print(s);
        super.flush();
    }

    public void println() {
        super.println();
        super.flush();
    }

    public void println(boolean b) {
        super.println(b);
        super.flush();
    }

    public void println(char c) {
        super.println(c);
        super.flush();
    }

    public void println(char ca[]) {
        super.println(ca);
        super.flush();
    }

    public void println(double d) {
        super.println(d);
        super.flush();
    }

    public void println(float f) {
        super.println(f);
        super.flush();
    }

    public void println(int i) {
        super.println(i);
        super.flush();
    }

    public void println(long l) {
        super.println(l);
        super.flush();
    }

    public void println(Object o) {
        super.println(o);
        super.flush();
    }

    public void println(String s) {
        super.println(s);
        super.flush();
    }

    public void write(char c) {
        super.write(c);
        super.flush();
    }

    public void write(char ca[]) {
        super.write(ca);
        super.flush();
    }

    public void write(char ca[], int off, int len) {
        super.write(ca, off, len);
        super.flush();
    }

    public void write(String s) {
        super.write(s);
        super.flush();
    }

    public void write(String s, int off, int len) {
        super.write(s, off, len);
        super.flush();
    }

}
