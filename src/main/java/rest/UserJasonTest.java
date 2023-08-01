package rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class UserJasonTest {
//
//	@BeforeClass
//	public static void setup() {
//		// Desabilitar a verificação do certificado SSL
//		RestAssured.useRelaxedHTTPSValidation();
//	}

	@Test
	public void deveVerificarPrimeiroNivel() {
		given().when().get("https://restapi.wcaquino.me/users/1").then().statusCode(200).body("id", is(1))
				.body("name", containsString("Silva")).body("age", greaterThan(18));
	}

	@Test
	public void deveVerificarPrimeiroNivelDeOutrasFormas() {
		Response response = RestAssured.request(Method.GET, "https://restapi.wcaquino.me/users/1");

		// Path
		Assert.assertEquals(new Integer(1), response.path("id"));
		Assert.assertEquals(new Integer(1), response.path("%s", "id"));

		// Jsonpath
		JsonPath jpath = new JsonPath(response.asString());
		Assert.assertEquals(1, jpath.getInt("id"));

		// From
		int id = JsonPath.from(response.asString()).getInt("id");
		Assert.assertEquals(id, 1);
	}

	@Test
	public void deveVerificarSegundoNivel() {
		given().when().get("https://restapi.wcaquino.me/users/2").then().statusCode(200)
				.body("name", containsString("Joaquina")).body("endereco.rua", is("Rua dos bobos"));
	}

	@Test
	public void deveVerificarLista() {
		given().when().get("https://restapi.wcaquino.me/users/3").then().statusCode(200)
				.body("name", containsString("Ana")).body("filhos", hasSize(2)).body("filhos[0].name", is("Zezinho"))
				.body("filhos[1].name", is("Luizinho")).body("filhos.name", hasItem("Zezinho"))
				.body("filhos.name", hasItems("Zezinho", "Luizinho"));
	}

	public void deveVerificarErrorUsuarioInexistente() {
		given().when().get("https://restapi.wcaquino.me/users/4").then().statusCode(404);

	}

	@Test
	public void deveVerificarListaRaiz() {
		given().when().get("https://restapi.wcaquino.me/users").then().statusCode(200).body("$", hasSize(3))
				.body("name", hasItems("João da Silva", "Maria Joaquina", "Ana Júlia")).body("age[1]", is(25))
				.body("filhos.name", hasItem(Arrays.asList("Zezinho", "Luizinho")))
				.body("salary", contains(1234.5677f, 2500, null));
	}

	@Test
	public void deveVerificacoesAvancadas() {
		given().when().get("https://restapi.wcaquino.me/users").then().statusCode(200).body("$", hasSize(3))
				.body("age.findAll{it<=25}.size()", is(2)).body("age.findAll{it<=25&&it>20}.size()", is(1))
				.body("findAll{it.age <=25 && it.age>20}.name", hasItem("Maria Joaquina"))
				.body("findAll{it.age<=25}[0].name", is("Maria Joaquina"))
				.body("findAll{it.age<=25}[-1].name", is("Ana Júlia"))
				.body("find{it.age<=25}.name", is("Maria Joaquina"))
				.body("findAll{it.name.contains('n')}.name", hasItems("Maria Joaquina", "Ana Júlia"))
				.body("findAll{it.name.length()>10}.name", hasItems("João da Silva", "Maria Joaquina"))
				.body("name.collect{it.toUpperCase()}", hasItem("MARIA JOAQUINA"))
				.body("name.findAll{it.startsWith('Maria')}.collect{it.toUpperCase()}", hasItem("MARIA JOAQUINA"))
				.body("name.findAll{it.startsWith('Maria')}.collect{it.toUpperCase()}.toArray()",
						anyOf(arrayContaining("MARIA jOAQUINA"), arrayWithSize(1)))
				.body("age.collect{it*2}", hasItems(60, 50, 40)).body("id.max()", is(3))
				.body("salary.min()", is(1234.5678f))
				.body("salary.findAll{it !=null}.sum()", is(closeTo(3734.5678f, 0.001)))
				.body("salary.findAll{it !=null}.sum()", allOf(greaterThan(3000d), lessThan(5000d)));

	}

}
