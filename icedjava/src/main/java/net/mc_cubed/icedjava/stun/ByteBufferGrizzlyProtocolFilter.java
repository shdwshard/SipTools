/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author charles
 */
public class ByteBufferGrizzlyProtocolFilter extends BaseFilter {

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
