package hluo.fun.playground.psi.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

public class IdHelper
{
    private static final Pattern ID_PATTERN = Pattern.compile("[_a-z0-9]+");

    public static String validateId(String id)
    {
        requireNonNull(id, "id is null");
        checkArgument(!id.isEmpty(), "id is empty");
        checkArgument(ID_PATTERN.matcher(id).matches(), "Invalid id %s", id);
        return id;
    }

    public static List<String> parseDottedId(String id, int expectedParts, String name)
    {
        requireNonNull(id, "id is null");
        checkArgument(expectedParts > 0, "expectedParts must be at least 1");
        requireNonNull(name, "name is null");

        List<String> ids = unmodifiableList(Arrays.asList(id.split("\\.")));
        checkArgument(ids.size() == expectedParts, "Invalid %s %s", name, id);

        for (String part : ids) {
            checkArgument(!part.isEmpty(), "Invalid id %s", id);
            checkArgument(ID_PATTERN.matcher(part).matches(), "Invalid id %s", id);
        }
        return ids;
    }
}
