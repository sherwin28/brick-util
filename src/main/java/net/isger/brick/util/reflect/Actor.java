package net.isger.brick.util.reflect;

import java.util.Vector;

import net.isger.brick.util.hitcher.Director;

public class Actor extends Director {

    // private static final Logger LOG;

    private static final Actor ACTOR = new Actor();

    private static final String KEY_ACTIONS = "brick.util.reflect.actions";

    private static final String ACTION_PATH = "net/isger/brick/util/reflect/action";

    private Vector<Action> actions;

    // static {
    // LOG = LoggerFactory.getLogger(Actor.class);
    // }

    protected Actor() {
        actions = new Vector<Action>();
    }

    protected String directHitchPath() {
        return directHitchPath(KEY_ACTIONS, ACTION_PATH);
    }

    protected void directSanity() {
    }

    public static Actor getActor() {
        return canonicalize(ACTOR);
    }

    public static Object act(Class<?> clazz) {

        return null;
    }

    public void add(Action action) {
        actions.add(action);
    }
}
