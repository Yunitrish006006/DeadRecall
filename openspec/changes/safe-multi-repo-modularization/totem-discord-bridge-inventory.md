# TotemDiscordBridge ownership inventory

| Surface | Current owner path |
|---|---|
| Server bootstrap and command/runtime hooks | `bootstrap/TotemDiscordBridgeBootstrap.java` |
| Transport, configuration and event formatting | `DiscordBridge.java`, `discord/**` |
| Payload types and receivers | `network/{Discord*Payload,ManageDiscordChannelPayload,RequestDiscordConfigPayload,SaveDiscordConfigPayload}` and `network/registration/TotemDiscordBridgePayloadRegistration.java` |
| Client configuration UI/bootstrap/datagen | `client/{DiscordConfigScreen,TotemDiscordBridgeClientBootstrap}.java`, `client/datagen/TotemDiscordBridgeDataGeneration.java` |
| Mixins | `mixin/{DiscordBridgeTransientNotificationMixin,DiscordMixinFormatting}.java`, `mixin/client/DiscordBridgeOptionsMixin.java`, `deadrecall.discord*.mixins.json` |
| Language resources | `assets/deadrecall/lang/discord_zh_tw/**` |

All resources and payload identifiers retain the `deadrecall` namespace during
the bundle observation window. The filtered-history branch records the source
history; a later copy commit will add the required build scaffold and exact
Core dependency before standalone validation.
