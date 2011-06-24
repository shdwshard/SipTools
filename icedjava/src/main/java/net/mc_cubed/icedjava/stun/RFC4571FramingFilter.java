/*
 * Copyright 2011 Charles Chappell.
 *
 * This file is part of IcedJava.
 *
 * IcedJava is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * IcedJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with IcedJava.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package net.mc_cubed.icedjava.stun;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

/**
 * This filter breaks buffer/datagram based data up into frames suitable for
 * transmission over a stream oriented connection
 *
 * @author Charles Chappell
 * @since 1.0
 */
public class RFC4571FramingFilter extends BaseFilter {

    /**
     * Buffer for holding incomplete read data.  Writes are always performed 
     * completely, so this is only for reads
     */
    ByteBuffer readBuffer = ByteBuffer.allocate(Short.MAX_VALUE + 2);
    int expectedBytes = 0;

    /**
     * Assumes ByteBuffer formatted input
     * 
     * @param ctx
     * @return
     * @throws IOException 
     */
    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {
        ByteBuffer inBuffer = ctx.getMessage();

        /**
         * If we had an incomplete packet last time, we should continue reading
         * it now
         */
        if (readBuffer.position() > 0) {
            readBuffer.put(inBuffer);
            // If we have enough bytes to proceed, do so now
            if (readBuffer.position() >= expectedBytes) {
                // Flip the buffer for reading
                readBuffer.flip();
                // Allocate an appropriately sized output buffer
                ByteBuffer outBuffer = ByteBuffer.allocate(expectedBytes);
                // Write the bytes into the output buffer
                outBuffer.put(readBuffer.array(), readBuffer.arrayOffset() + readBuffer.position(), expectedBytes);
                // Forward by the number of bytes read
                readBuffer.position(readBuffer.position() + expectedBytes);
                // Set the output buffer
                ctx.setMessage(outBuffer);
                /*
                 * What to do next?  If readBuffer has bytes remaining, then we
                 * should compact and iterate again, otherwise, clear the read
                 * buffer and proceed
                 */
                if (readBuffer.remaining() > 0) {
                    // compact buffer and read again
                    readBuffer.compact();
                    // Flip the buffer into write mode
                    readBuffer.flip();
                    // Invoke next filter and iterate again
                    return ctx.getInvokeAction(ByteBuffer.allocate(0));
                } else {
                    // Clear the filter
                    readBuffer.clear();
                    // Invoke the next filter
                    return ctx.getInvokeAction();
                }
            } else {
                // We don't yet have enough data to form a complete buffer, so stop here
                return ctx.getStopAction();
            }
        } else {
            // Peek at the first two bytes to see how many additional bytes we need
            expectedBytes = inBuffer.getShort();

            // Check whether we have the requisite number of bytes
            if (inBuffer.remaining() >= expectedBytes) {
                // Allocate a buffer to hold the incoming data
                ByteBuffer outBuffer = ByteBuffer.allocate(expectedBytes);
                // Read the complete chunk
                outBuffer.put(inBuffer.array(), inBuffer.arrayOffset() + inBuffer.position(), expectedBytes);
                // Increment the input buffer by the number of bytes read
                inBuffer.position(expectedBytes + inBuffer.position());
                // Set the resulting buffer for passing upstream
                ctx.setMessage(outBuffer);
                /*
                 * What to do next?  If inBuffer has bytes remaining, then we
                 * should iterate again, otherwise, just pass the buffer up
                 */
                if (inBuffer.remaining() > 0) {
                    // Invoke next filter and iterate again
                    return ctx.getInvokeAction(inBuffer);
                } else {
                    // Clear the filter
                    readBuffer.clear();
                    // Invoke the next filter
                    return ctx.getInvokeAction();
                }
            } else {
                // Put the bytes into the read buffer for later processing
                readBuffer.put(inBuffer);
                // Stop filter chain execution until more data arrives
                return ctx.getStopAction();
            }

        }
    }

    /**
     * Assumes ByteBuffer formatted input, and frames the RTP/RTCP data for
     * sending over a stream oriented connection
     * 
     * @param ctx
     * @return
     * @throws IOException 
     */
    @Override
    public NextAction handleWrite(FilterChainContext ctx) throws IOException {
        // Get the input data
        ByteBuffer bb = ctx.getMessage();
        // Check to make sure it's not too long
        int writeBytes = Math.min(bb.remaining(), Short.MAX_VALUE);

        // Allocate an output buffer to hold the data
        ByteBuffer outBuffer = ByteBuffer.allocate(writeBytes + 2);
        // Put the length field to start the framing
        outBuffer.putShort((short) writeBytes);
        // Put the actual frame data
        outBuffer.put(bb.array(), bb.arrayOffset() + bb.position(), writeBytes);
        // Forward the input buffer by writeBytes
        bb.position(bb.position() + writeBytes);
        // Flip the output buffer for reading
        outBuffer.flip();

        // Output the buffer to the network
        ctx.setMessage(outBuffer);
        // If no bytes remain, chain to the next filter only
        // If bytes remain, re-invoke with the remaining data
        if (bb.remaining() > 0) {
            // Chain and re-invoke with remaining data
            return ctx.getInvokeAction(bb);
        } else {
            // Chain to the next filter
            return ctx.getInvokeAction();
        }
    }
}
