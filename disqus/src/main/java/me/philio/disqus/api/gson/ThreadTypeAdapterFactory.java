package me.philio.disqus.api.gson;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import me.philio.disqus.api.model.forums.Forum;
import me.philio.disqus.api.model.threads.Thread;
import me.philio.disqus.api.model.users.User;

/**
 * A {@link TypeAdapterFactory} to handle different {@link Thread} responses
 */
public class ThreadTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        // Return null if not a thread object
        if (!type.getType().equals(Thread.class)) {
            return null;
        }

        // Get adapters
        final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
        final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
        final TypeAdapter<Forum> forumAdapter = gson.getAdapter(Forum.class);
        final TypeAdapter<User> userAdapter = gson.getAdapter(User.class);

        // Return adapter
        return new TypeAdapter<T>() {

            @Override
            public void write(JsonWriter out, T value) throws IOException {
                delegate.write(out, value);
            }

            @Override
            public T read(JsonReader in) throws IOException {
                JsonElement jsonTree = elementAdapter.read(in);
                JsonElement forum = jsonTree.getAsJsonObject().get("forum");
                JsonElement author = jsonTree.getAsJsonObject().get("author");

                // Process the thread with the delegate
                T thread = delegate.fromJsonTree(jsonTree);

                // Process forum and author if needed
                if (forum.isJsonObject()) {
                    ((Thread) thread).forum = forumAdapter.fromJsonTree(forum);
                }
                if (author.isJsonObject()) {
                    ((Thread) thread).author = userAdapter.fromJsonTree(author);
                }

                // Return thread
                return thread;
            }

        };

    }

}
