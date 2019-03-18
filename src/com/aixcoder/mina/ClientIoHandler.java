package com.aixcoder.mina;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.widgets.Display;

import com.aixcoder.extension.AiXSortUIIJob;
import com.aixcoder.lib.JSON;
import com.aixcoder.lib.Preference;
import com.aixcoder.utils.Pair;
import com.nnthink.aixcoder.mina.Message;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientIoHandler extends SimpleChannelInboundHandler<Message> {

	public static ITextViewer viewer;

	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message msg) throws Exception {
		System.out.println("server - >" + msg.toJsonString());
		switch (msg.getType()) {
		case Message.sort:
			sort(msg);
			break;
		case Message.notice:
			;
			break;
		case Message.active:
			channelHandlerContext.channel()
					.writeAndFlush(Message.build().setType(Message.active).setData(Preference.getUUID()));
			break;
		default:
			channelHandlerContext.channel().closeFuture();
		}
	}

	public void sort(Message message) {
		try {
			JSON json = JSON.decode(message.getData());
			List<JSON> array = json.getList();
			List<Pair<Double, String>> list = new ArrayList<Pair<Double, String>>();
			for (JSON j : array) {
				List<JSON> pair = j.getList();
				Double prob = pair.get(0).getDouble();
				String word = pair.get(1).getString();
				list.add(new Pair<Double, String>(prob, word));
			}
			new AiXSortUIIJob(Display.getDefault(), viewer, list).schedule();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
