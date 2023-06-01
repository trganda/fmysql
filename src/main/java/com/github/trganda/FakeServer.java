package com.github.trganda;

import com.github.trganda.codec.decoder.MySQLClientConnectionPacketDecoder;
import com.github.trganda.codec.encoder.MySQLServerPacketEncoder;
import com.github.trganda.engine.NormalSQLEngine;
import com.github.trganda.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FakeServer implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(FakeServer.class);
    private final int port;
    private final String user = "poc";
    private final Channel channel;
    private final EventLoopGroup parentGroup;
    private final EventLoopGroup childGroup;
    private final String password = "passwd";

    public FakeServer(int port) {
        this.port = port;
        parentGroup = new NioEventLoopGroup();
        childGroup = new NioEventLoopGroup();
        final ChannelFuture channelFuture =
                new ServerBootstrap()
                        .group(parentGroup, childGroup)
                        .channel(NioServerSocketChannel.class)
                        .handler(new LoggingHandler(LogLevel.INFO))
                        .childHandler(
                                new ChannelInitializer<NioSocketChannel>() {
                                    @Override
                                    protected void initChannel(NioSocketChannel ch)
                                            throws Exception {
                                        final ChannelPipeline pipeline = ch.pipeline();
                                        pipeline.addLast("encoder", new MySQLServerPacketEncoder());
                                        pipeline.addLast(
                                                "decoder",
                                                new MySQLClientConnectionPacketDecoder());
                                        pipeline.addLast(
                                                "handler",
                                                new ServerHandler(
                                                        new NormalSQLEngine(user, password)));
                                    }
                                })
                        .bind(port);
        channel = channelFuture.channel();
        channelFuture.awaitUninterruptibly();
    }

    @Override
    public void close() {
        channel.close();
        childGroup.shutdownGracefully().awaitUninterruptibly();
        parentGroup.shutdownGracefully().awaitUninterruptibly();
    }

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public String getUser() {
        return user;
    }
}
