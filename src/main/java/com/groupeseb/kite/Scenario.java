package com.groupeseb.kite;

import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.json.simple.parser.ParseException;
import org.springframework.core.io.ClassPathResource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Possible root values :
 * <p/>
 * commands:[CommandObject]
 * dependencies:["urlToAnotherTest"]
 * description:"Description"
 * variables: {
 * "variableName":"variableValue"
 * }
 * objectVariables: {
 * "jwtVariableName":{jwtVariableValueAsJsonObject}
 * }
 * <p/>
 * For variables use, see {@link com.groupeseb.kite.function.impl.VariableFunction}
 * For objectVariables use, see {@link com.groupeseb.kite.function.impl.JwtFunction}
 */
@Getter
public class Scenario {
	public static final String DESCRIPTION_KEY = "description";
	public static final String VARIABLE_KEY = "variables";
	public static final String COMMANDS_KEY = "commands";
	public static final String DEPENDENCIES_KEY = "dependencies";
	public static final String OBJECTS_KEY = "objectVariables";

	private final Collection<Command> commands = new ArrayList<>();
	private final List<Scenario> dependencies = new ArrayList<>();
	private String description;
	private Map<String, Object> variables;
	private Map<String, Object> objectVariables;

	private final String filename;

	/**
	 * @param filename The (class)path to the scenario file.
	 * @throws IOException
	 * @throws ParseException
	 */
	public Scenario(String filename) throws IOException, ParseException {
		this.filename = filename;
		parseScenario(readFixture(filename));
	}

	protected static String readFixture(String filename) throws IOException {
		ClassPathResource resource = new ClassPathResource(filename);

		if (!resource.exists()) {
			throw new FileNotFoundException(filename);
		}

		InputStream inputStream = resource.getInputStream();
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer);

		return writer.toString();
	}

	@SuppressWarnings("unchecked")
	private void parseScenario(String scenario) throws IOException, ParseException {
		Json jsonScenario = new Json(scenario);
		jsonScenario.checkExistence(new String[]{DESCRIPTION_KEY, COMMANDS_KEY});

		this.description = jsonScenario.getString(DESCRIPTION_KEY);
		this.variables = (Map<String, Object>) jsonScenario.getMap(VARIABLE_KEY);

		this.objectVariables = (Map<String, Object>) jsonScenario.getMap(OBJECTS_KEY);

		for (String dependency : jsonScenario.<String>getIterable(DEPENDENCIES_KEY)) {
			dependencies.add(new Scenario(dependency));
		}

		Integer commandCount = jsonScenario.getLength(COMMANDS_KEY);
		for (Integer i = 0; i < commandCount; ++i) {
			commands.add(new Command(jsonScenario.get(COMMANDS_KEY).get(i)));
		}
	}

	@Override
	public String toString() {
		return this.getFilename() + ':' + this.getDescription();
	}
}
