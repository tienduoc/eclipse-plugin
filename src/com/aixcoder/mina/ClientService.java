package com.aixcoder.mina;

import com.aixcoder.lib.Preference;
import com.nnthink.aixcoder.mina.Message;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class ClientService {
	static EventLoopGroup workerGroup = new NioEventLoopGroup();
	static boolean started = false;
	static String endpoint = "";
	static int port = 0;

	public synchronized static void start() {
		if (started && Preference.getSocketEndpoint().equals(endpoint) && Preference.getSocketEndpointPort() == port)
			return;
		started = true;
		Bootstrap b = new Bootstrap(); // (1)
		b.group(workerGroup); // (2)
		b.channel(NioSocketChannel.class); // (3)
		b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
		b.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new ObjectEncoder());
				ch.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE,
						ClassResolvers.cacheDisabled(Message.class.getClassLoader())));
				ch.pipeline().addLast(new ClientIoHandler());
			}
		});
		ChannelFuture future = null;
		try {
			future = b.connect(Preference.getSocketEndpoint(), Preference.getSocketEndpointPort()).sync();
			endpoint = Preference.getSocketEndpoint();
			port = Preference.getSocketEndpointPort();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (future.isSuccess()) {
			ChannelFuture a = future.channel()
					.writeAndFlush(Message.build().setType(Message.login).setData(Preference.getUUID()));
			a.addListener(new GenericFutureListener<Future<Void>>() {
				@Override
				public void operationComplete(Future<Void> arg0) throws Exception {
					if (arg0.isSuccess()) {
						System.out.println("isSuccess");
						System.out.println("UUID sent: " + Preference.getUUID());
					} else {
						System.out.println("failed");
						started = false;
					}
				}
			});
		}
	}

	public void restart() {

	}
}
