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
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.memory.ByteBufferWrapper;

/**
 * Translates from Grizzly Buffers into NIO buffers and vice versa
 * 
 * Typically will not be used directly, but instead as simply a Wizard of Oz
 * type entity hiding behind the curtains.
 * 
 * @author Charles Chappell
 * @since 1.0
 */
class ByteBufferGrizzlyProtocolFilter extends BaseFilter {

    // Take a grizzly buffer, and output a ByteBuffer
    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {
        if (ctx.getMessage() instanceof Buffer) {
            Buffer buffer = ctx.getMessage();
            
            // Allocate an appropriately sized ByteBuffer
            ByteBuffer dst = ByteBuffer.allocate(buffer.capacity());
            buffer.get(dst);
            dst.flip();
            ctx.setMessage(dst);
            
        }
        return super.handleRead(ctx);
    }

    // Take a ByteBuffer and output a Grizzly buffer
    @Override
    public NextAction handleWrite(FilterChainContext ctx) throws IOException {
        if (ctx.getMessage() instanceof ByteBuffer) {
            ctx.setMessage(new ByteBufferWrapper((ByteBuffer)ctx.getMessage()));
        }
        return super.handleWrite(ctx);
    }
    
}
