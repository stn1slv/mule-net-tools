package com.mulesoft.tool.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.ServerSocket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class NetworkUtilsTest {

	@ParameterizedTest
	@ValueSource(strings = {"example.com", "erp.internal.example.com", "10.20.30.40", "::1", "2001:db8::1", "a-b.c"})
	void acceptsHostNamesAndAddresses(String host) {
		assertTrue(NetworkUtils.isValidHost(host));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"-f/etc/passwd", "+short", "-c", ".example.com", "a b", "a;b", "a\nb", "host/path"})
	void rejectsValuesThatCouldBeReadAsOptions(String host) {
		assertFalse(NetworkUtils.isValidHost(host));
	}

	@Test
	void rejectsHostsLongerThanADomainName() {
		assertFalse(NetworkUtils.isValidHost("a".repeat(254)));
		assertTrue(NetworkUtils.isValidHost("a".repeat(253)));
	}

	@Test
	void pingRejectsOptionLikeHost() throws Exception {
		assertTrue(NetworkUtils.ping("-f/etc/passwd").startsWith("Invalid host"));
	}

	@Test
	void traceRouteRejectsOptionLikeHost() throws Exception {
		assertTrue(NetworkUtils.traceRoute("-F").startsWith("Invalid host"));
	}

	@Test
	void resolveIPsRejectsOptionLikeHostAndDnsServer() throws Exception {
		assertTrue(NetworkUtils.resolveIPs("-f/etc/passwd", "10.0.0.2").startsWith("Invalid host"));
		assertTrue(NetworkUtils.resolveIPs("example.com", "+tcp").startsWith("Invalid DNS server"));
	}

	@Test
	void resolveIPsAcceptsNullDnsServer() throws Exception {
		assertEquals("127.0.0.1", NetworkUtils.resolveIPs("127.0.0.1", null));
	}

	@Test
	void testConnectReportsRefusedPortWithoutStackTrace() throws Exception {
		int port;
		try (ServerSocket server = new ServerSocket(0)) {
			port = server.getLocalPort();
		}
		String result = NetworkUtils.testConnect("127.0.0.1", Integer.toString(port));
		assertTrue(result.startsWith("Could not connect to 127.0.0.1:" + port), result);
		assertFalse(result.contains("\tat "), result);
	}
}
