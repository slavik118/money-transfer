package com.al.mt;

import static spark.Spark.afterAfter;
import static spark.Spark.awaitInitialization;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.internalServerError;
import static spark.Spark.notFound;
import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.post;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.al.mt.aggregates.AccountEventStorage;
import com.al.mt.aggregates.EventManager;
import com.al.mt.controllers.AccountController;
import com.al.mt.enums.Status;
import com.al.mt.filters.CORSFilter;
import com.al.mt.filters.JsonBodyFilter;
import com.al.mt.filters.JsonContentTypeFilter;
import com.al.mt.filters.LoggingFilter;
import com.al.mt.model.APIResponse;
import com.al.mt.services.AccountService;
import com.al.mt.services.AccountServiceImpl;
import com.al.mt.utils.JsonUtils;
import com.google.common.eventbus.EventBus;

import static com.al.mt.utils.Constants.PORT;

/**
 * Runs HTTP server on port 8000;
 *
 * <p>
 * This microservice allows - to create account - list existing accounts - get
 * given account - transfer money from one account to another
 */
public class MainApp {
	private final static Logger LOG = LoggerFactory.getLogger(MainApp.class);

	private static final EventBus EVENT_BUS = new EventBus();
	public static final AccountEventStorage ACCOUNT_EVENT_STORAGE = new AccountEventStorage();
	private static final EventManager EVENT_MANAGER = new EventManager(EVENT_BUS, ACCOUNT_EVENT_STORAGE);
	private static final AccountService ACCOUNT_SERVICE = new AccountServiceImpl(EVENT_BUS);
	private static final AccountController ACCOUNT_CONTROLLER = new AccountController(ACCOUNT_SERVICE,
			ACCOUNT_EVENT_STORAGE);

	public static void main(String[] args) {
		port(PORT);

		// Registers event listener to EventBus
		EVENT_BUS.register(EVENT_MANAGER);

		// Before filter
		before(new LoggingFilter());
		before("/api/*", new JsonBodyFilter());

		// After filters
		afterAfter(new JsonContentTypeFilter());
		afterAfter(new CORSFilter());

		// Controllers
		path("", () -> {
			path("/api", () -> path("/account", () -> {
				get("", ACCOUNT_CONTROLLER.listAccounts(), JsonUtils::toJson);
				post("", ACCOUNT_CONTROLLER.createAccount(), JsonUtils::toJson);
				get("/:id", ACCOUNT_CONTROLLER.getAccount(), JsonUtils::toJson);
				post("/transferMoney", ACCOUNT_CONTROLLER.transferMoney(), JsonUtils::toJson);
			}));
		});

		// Other handlers
		notFound((request, response) -> APIResponse.builder()
				.setStatus(Status.ERROR)
				.setMessage("Requested resource doesn't exist")
				.build()
				.toJson());
		internalServerError((request, response) -> APIResponse.builder()
				.setStatus(Status.ERROR)
				.setMessage("Internal Server Error")
				.build()
				.toJson());

		awaitInitialization();
		logMessage();
	}

	private static void logMessage() {
		LOG.info("***************************************");
		LOG.info("*** Server is running on port: {} ***", PORT);
		LOG.info("***************************************");
	}

}
