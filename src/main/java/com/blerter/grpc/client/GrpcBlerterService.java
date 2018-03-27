package com.blerter.grpc.client;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.blerter.constant.Status;
import com.blerter.grpc.service.BlerterServiceGrpc;
import com.blerter.model.Empty;
import com.blerter.model.Response;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.util.RoundRobinLoadBalancerFactory;

/**
 * Grpc master service client
 *
 */
public final class GrpcBlerterService {
	private static Logger logger = LogManager.getLogger(GrpcBlerterService.class);
	private transient BlerterServiceGrpc.BlerterServiceBlockingStub blerterServiceBlockingStub;
	private transient final Timer timerPingHost = new Timer();
	private transient ManagedChannel channel;
	private static GrpcBlerterService instance;
	private String grpcHost;
	private String grpcPort;

	private GrpcBlerterService() {
		grpcHost = System.getenv("BLERTER_SERVICE_HOST");
		grpcPort = System.getenv("BLERTER_SERVICE_PORT");
		load();
	}

	/**
	 * Instance
	 */
	public static GrpcBlerterService client() {
		if (instance == null) {
			instance = new GrpcBlerterService();
		}
		return instance;
	}

	/**
	 * Generate token
	 * @param request
	 */
	public Response generateToken(com.blerter.grpc.service.Request request) {
		Response response = null;
		try {
			response = blerterServiceBlockingStub.generateToken(request);
	
		} catch (Exception exc) {
			// fatal exception encountered, reset service blocking stub
			shutdown();
			Response.Builder responseBuilder = Response.newBuilder();
			responseBuilder.setResponseCode(com.blerter.constant.Status.InternalServerError.value());
			response = responseBuilder.build();
		}
		return response;
	}
	
	public Response checkToken(com.blerter.grpc.service.Request request) {
		Response response = null;
		try {
			response = blerterServiceBlockingStub.checkToken(request);
	
		} catch (Exception exc) {
			// fatal exception encountered, reset service blocking stub
			shutdown();
			Response.Builder responseBuilder = Response.newBuilder();
			responseBuilder.setResponseCode(com.blerter.constant.Status.InternalServerError.value());
			response = responseBuilder.build();
		}
		return response;
	}
	
	/**
	 * Get health
	 * @param request
	 */
	public Response getHealth(com.blerter.grpc.service.Request request) {
		Response response = null;
		try {
			response = blerterServiceBlockingStub.getHealth(request);
	
		} catch (Exception exc) {
			// fatal exception encountered, reset service blocking stub
			shutdown();
			Response.Builder responseBuilder = Response.newBuilder();
			responseBuilder.setResponseCode(com.blerter.constant.Status.InternalServerError.value());
			response = responseBuilder.build();
		}
		return response;
	}
	
	/**
	 * Ping
	 */
	public Response ping() {
		Response response = null;
		try {
			Empty.Builder builder = Empty.newBuilder();
			response = blerterServiceBlockingStub.ping(builder.build());

		} catch (Exception exc) {
			// fatal exception encountered, reset service blocking stub
			shutdown();
			Response.Builder responseBuilder = Response.newBuilder();
			responseBuilder.setResponseCode(com.blerter.constant.Status.InternalServerError.value());
			response = responseBuilder.build();
		}
		return response;
	}

	/**
	 * Load
	 */
	private void load() {
		logger.info("-------------------------------------------------");
		logger.info("grpc.blerter.server:" + grpcHost + ":" + grpcPort);
		logger.info("-------------------------------------------------");
		if (grpcHost != null && !grpcHost.contains("tcp") && grpcPort != null) {
			// initialise
			initialise();

			// schedule to run 15s.
			timerPingHost.schedule(new PingHostTask(), 0, 1000 * 15);
		}
	}

	/**
	 * Initialise
	 */
	private void initialise() {
		try {
			channel = ManagedChannelBuilder.forAddress(grpcHost, Integer.parseInt(grpcPort))
					.loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance()).usePlaintext(true).build();
			blerterServiceBlockingStub = BlerterServiceGrpc.newBlockingStub(channel);
	
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	/**
	 * Ping host task
	 *
	 */
	class PingHostTask extends TimerTask {

		/**
		 * Constructor.
		 */
		public PingHostTask() {
			logger.info("PingHostTask{GrpcMasterService} started ...");
		}

		/**
		 * Run.
		 */
		public void run() {
			Response response = ping();
			if (response.getResponseCode() == Status.Ok.value()) {
				if (blerterServiceBlockingStub == null) {
					initialise();
				}
			} else {
				initialise();
			}
		}
	}

	/**
	 * Shutdown
	 */
	private void shutdown() {
		try {
			channel.shutdownNow();
			channel.awaitTermination(45, TimeUnit.SECONDS);
			if (blerterServiceBlockingStub != null) {
				logger.info("grpc.master.server disconnected ...");
			}
			blerterServiceBlockingStub = null;
			
		} catch (Exception exc) {
			logger.error(exc.getMessage());
		}
	}
}


