package it.vige.webprogramming.servletjsp.nonblocking;

import static java.lang.System.out;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

public class ReadingListener implements ReadListener {

	private ServletInputStream input = null;
	private AsyncContext context = null;

	public ReadingListener(ServletInputStream in, AsyncContext ac) {
		this.input = in;
		this.context = ac;
	}

	@Override
	public void onDataAvailable() {
		try {
			int len = -1;
			byte b[] = new byte[1024];
			while (input.isReady() && (len = input.read(b)) != -1) {
				String data = new String(b, 0, len);
				out.println("--> " + data);
			}
		} catch (IOException ex) {
			getLogger(ReadingListener.class.getName()).log(SEVERE, null, ex);
		}
	}

	@Override
	public void onAllDataRead() {
		out.println("onAllDataRead");
		context.complete();
	}

	@Override
	public void onError(Throwable t) {
		t.printStackTrace();
		context.complete();
	}
}