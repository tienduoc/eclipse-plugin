package com.aixcoder.mina;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.widgets.Display;

import com.aixcoder.extension.AiXSortUIIJob;
import com.aixcoder.lib.Preference;
import com.aixcoder.utils.Pair;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nnthink.aixcoder.mina.Message;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientIoHandler extends SimpleChannelInboundHandler<Message> {

	public static ITextViewer viewer;

	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message msg) throws Exception {
//		System.out.println("server - >" + msg.toJsonString());
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
			String data = message.getData();
			JsonObject json = new Gson().fromJson(data, JsonObject.class);
			String uuid = json.get("queryUUID").getAsString();
			JsonArray array = json.get("list").getAsJsonArray();
			List<Pair<Double, String>> list = new ArrayList<Pair<Double, String>>();
			for (JsonElement j : array) {
				JsonArray pair = j.getAsJsonArray();
				Double prob = pair.get(0).getAsDouble();
				String word = pair.get(1).getAsString();
				list.add(new Pair<Double, String>(prob, word));
			}
			new AiXSortUIIJob(Display.getDefault(), viewer, list, null, uuid).schedule();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		ClientService.started = false;
	}
}
