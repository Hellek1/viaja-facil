package eu.hellek.viajafacil.android;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;
import eu.hellek.gba.proto.Helpers;
import eu.hellek.gba.proto.RequestsProtos.IndirectSearchRequest;
import eu.hellek.gba.proto.SearchResultProtos.SearchResultProxy;
import eu.hellek.viajafacil.android.ViajaFacilActivity.IndirectSearchTask;

/*
 * similar to DirectSearchTread, this class runs an IndirectSearch task which means that it allows using several bus lines but the search takes longer
 * several IndirectSearchThreads might be running at the same time since the possible lines are divided into subsets that are evaluated in parallel.
 */
public class IndirectSearchThread implements Runnable {
	
	IndirectSearchTask parent;
	IndirectSearchRequest req;
	
	/*
	 * initiates the thread with the corresponding Async-Task as parent that receives the result and the reques ready to be transmitted
	 * The request has to contain the sets mlk1 and mlk2 with valid data obtained from a directsearchrequest!!
	 */
	public IndirectSearchThread(IndirectSearchTask parent, IndirectSearchRequest req) {
		this.parent = parent;
		this.req = req;
	}

	@Override
	public void run() {
		try {		
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost method = new HttpPost("https://"+ViajaFacilActivity.appurlssl+"/rm/IndirectSearchServlet");
			//HttpPost method = new HttpPost("http://"+appurlssl+"/rm/IndirectSearchServlet");
			ByteArrayEntity byteArrEntity = new ByteArrayEntity(req.toByteArray());
			byteArrEntity.setContentType("application/x-protobuf");
			method.setEntity(byteArrEntity);
			HttpResponse response = httpClient.execute(method);
			HttpEntity responseEntity = response.getEntity();
			SearchResultProxy srp = SearchResultProxy.parseFrom(responseEntity.getContent());
			parent.addResult(Helpers.copyFromProto(srp));
			Log.i("IndirectSearchThread", "A thread finished");
			parent.reportFinish();
		} catch(Exception e) {
			Log.e("IndirectSearchThread", "Error in indirect search", e);
			e.printStackTrace();
			parent.reportFinish();
		}
	}

}
