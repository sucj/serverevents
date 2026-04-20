# Server Events

[![GitHub License](https://img.shields.io/github/license/sucj/serverevents)](https://github.com/sucj/serverevents?tab=MIT-1-ov-file#readme)
[![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/sucj/serverevents/build.yml)](https://github.com/sucj/serverevents/actions/workflows/build.yml)
[![GitHub Release](https://img.shields.io/github/v/release/sucj/serverevents?include_prereleases)](https://github.com/sucj/serverevents/releases/latest)

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/serverevents?logo=modrinth)](https://modrinth.com/project/hykUQTdx)

**Server Events** is a support library for Fabric server development, designed to enhance the Fabric API's limited event system. It offers a Bukkit-like event framework while adhering to Fabric's minimalist philosophy.

The mod doesn't wrap `CommandRegistrationCallback` and `DynamicRegistrySetupCallback` from Fabric API.

## Installation
1. Import this package to your project.
2. Add `serverevents` to your mod depends.

```groovy
repositories {
    maven "https://mvn.suc.icu"
}

dependencies {
    implementation "icu.suc.mc:serverevents:<version>"
}
```

Since `2.0.0`, the **groupId** has been changed from `icu.suc` to `icu.suc.mc`.

## Usage
**ServerEvents** provides a simple API for registering and processing events.

Here is an example of a player modifying broadcast information:

```java
import net.fabricmc.api.ModInitializer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import icu.suc.mc.serverevents.ServerEvents;

public class ExampleMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerEvents.Player.MODIFY_JOIN_MESSAGE.register((player, message) ->
                Component.literal("[+] ").append(player.getName()));
    }
}
```

Since `2.2.0`:

```java
import icu.suc.mc.serverevents.Listener;
import icu.suc.mc.serverevents.ServerEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class ExampleMod implements ModInitializer, Listener, ServerEvents.Player.Join.ModifyMessage {
    @Override
    public void onInitialize() {
        ServerEvents.register(this);
    }

    @Override
    public @NotNull Event<?>[] events() {
        return new Event[]{ServerEvents.Player.Join.MODIFY_MESSAGE};
    }

    @Override
    public @NotNull Component modifyJoinMessage(@NotNull ServerPlayer player, @NotNull Component message) {
        return Component.literal("[+] ").append(player.getName());
    }
}
```

## License

This project is licensed under the [MIT License](LICENSE) © 2025 sucj.