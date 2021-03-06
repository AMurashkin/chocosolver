package org.clafer.javascript;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.clafer.ast.AstModel;
import org.clafer.scope.Scope;
import org.clafer.collection.Pair;

/**
 * This class provides various methods of loading models and scopes via the
 * Javascript API.
 *
 * @author jimmy
 */
public class Javascript {

    private Javascript() {
    }

    /**
     * @return a new Javascript engine.
     */
    public static ScriptEngine newEngine() {
        // The Rhino engine is currently the only supported engine.
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("rhino");
        if (engine == null) {
            throw new IllegalStateException("Missing Rhino Javascript engine.");
        }
        return engine;
    }

    public static Pair<AstModel, Scope> readModel(String in) throws ScriptException {
        return readModel("<unknown>", in, newEngine());
    }

    public static Pair<AstModel, Scope> readModel(File in) throws FileNotFoundException, ScriptException {
        return readModel(in, newEngine());
    }

    public static Pair<AstModel, Scope> readModel(Reader in) throws ScriptException {
        return readModel("<unknown>", in, newEngine());
    }

    public static Pair<AstModel, Scope> readModel(File in, ScriptEngine engine) throws FileNotFoundException, ScriptException {
        return readModel(in.getName(), in, engine);
    }

    public static Pair<AstModel, Scope> readModel(String name, String in, ScriptEngine engine) throws ScriptException {
        JavascriptContext context = new JavascriptContext();
        engine.put("rc", context);
        engine.put(ScriptEngine.FILENAME, "header.js");
        engine.eval(new InputStreamReader(Javascript.class.getResourceAsStream("header.js")));
        engine.put(ScriptEngine.FILENAME, name);
        engine.eval(in);
        return new Pair<AstModel, Scope>(context.getModel(), context.getScope());
    }

    public static Pair<AstModel, Scope> readModel(String name, File in, ScriptEngine engine) throws FileNotFoundException, ScriptException {
        return readModel(name, new FileReader(in), engine);
    }

    public static Pair<AstModel, Scope> readModel(String name, Reader in, ScriptEngine engine) throws ScriptException {
        JavascriptContext context = new JavascriptContext();
        engine.put("rc", context);
        engine.put(ScriptEngine.FILENAME, "header.js");
        engine.eval(new InputStreamReader(Javascript.class.getResourceAsStream("header.js")));
        engine.put(ScriptEngine.FILENAME, name);
        engine.eval(in);
        return new Pair<AstModel, Scope>(context.getModel(), context.getScope());
    }
}
