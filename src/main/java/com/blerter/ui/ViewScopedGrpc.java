package com.blerter.ui;

import javax.annotation.PostConstruct;

import com.blerter.grpc.client.GrpcBlerterService;
import com.blerter.grpc.service.Request;
import com.blerter.model.Id;
import com.blerter.model.Response;
import com.blerter.model.Token;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * ViewScoped grpc
 *
 */
@SuppressWarnings("serial")
@SpringView(name = ViewScopedGrpc.VIEW_NAME)
public class ViewScopedGrpc extends VerticalLayout implements View {
	public static final String VIEW_NAME = "view";
	private GrpcBlerterService grpcBlerterService = GrpcBlerterService.client();
	private transient String accessToken;

	/**
	 * init
	 */
	@PostConstruct
	void init() {
		
		VerticalLayout verticalLayout = new VerticalLayout();
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		TextField txtResponseCode = new TextField("Response Code");
		TextArea txtBody = new TextArea("Body");
		txtBody.setWidth("500px");
		Button btnCreateToken = new Button("Generated Token");
		btnCreateToken.setHeight("40px");
		btnCreateToken.setWidth("150px");
		btnCreateToken.addStyleName(ValoTheme.BUTTON_SMALL);
		btnCreateToken.addClickListener(event -> {
			Request.Builder requestBuilder = Request.newBuilder();
			Response response = grpcBlerterService.generateToken(requestBuilder.build());
			txtResponseCode.setValue(String.valueOf(response.getResponseCode()));
			txtBody.setValue(response.getInfo());
			if (response.getResponseCode() == com.blerter.constant.Status.Ok.value().intValue()) {
				accessToken = response.getInfo();
			}
		});

		Button btnValidateToken = new Button("Validate Token");
		btnValidateToken.setHeight("40px");
		btnValidateToken.setWidth("150px");
		btnValidateToken.addStyleName(ValoTheme.BUTTON_SMALL);
		btnValidateToken.addClickListener(event -> {
			Request.Builder requestBuilder = Request.newBuilder();
			Token.Builder tokenBuilder = Token.newBuilder();
			tokenBuilder.setToken(accessToken);
			requestBuilder.setToken(tokenBuilder.build());
			Response response = grpcBlerterService.checkToken(requestBuilder.build());
			txtResponseCode.setValue(String.valueOf(response.getResponseCode()));
			txtBody.setValue(response.getInfo());
		});

		Button btnGetHealth = new Button("Get Health");
		btnGetHealth.setHeight("40px");
		btnGetHealth.setWidth("150px");
		btnGetHealth.addStyleName(ValoTheme.BUTTON_SMALL);
		btnGetHealth.addClickListener(event -> {
			Request.Builder requestBuilder = Request.newBuilder();
			Token.Builder tokenBuilder = Token.newBuilder();
			tokenBuilder.setToken(accessToken);
			tokenBuilder.setToken(accessToken);
			requestBuilder.setToken(tokenBuilder.build());
			Id.Builder idBuilder = Id.newBuilder();
			idBuilder.setId(1);
			requestBuilder.setId(idBuilder.build());
			Response response = grpcBlerterService.getHealth(requestBuilder.build());
			txtResponseCode.setValue(String.valueOf(response.getResponseCode()));
			if (response.getResponseCode() == com.blerter.constant.Status.Ok.value().intValue()) {
				txtBody.setValue(response.getHealthList().get(0).toString());
			} else {
				txtBody.setValue(response.getInfo());
			}
		});

		horizontalLayout.addComponent(btnCreateToken);
		horizontalLayout.addComponent(btnValidateToken);
		horizontalLayout.addComponent(btnGetHealth);

		verticalLayout.addComponent(horizontalLayout);
		verticalLayout.addComponent(txtResponseCode);
		verticalLayout.addComponent(txtBody);

		addComponent(verticalLayout);

	}

	@Override
	public void enter(ViewChangeEvent event) {
		// This view is constructed in the init() method()
	}
}

