# PayPlayer

## Project Identity
- **Name:** PayPlayer
- **Mod ID:** `payplayer`
- **Version:** `${version}` (Resolved at build time)

## Technical Summary
The **PayPlayer** mod implements a physical item-based economy interaction system. Rather than relying entirely on chat commands, it registers two unique interactive items (`charge_money` and `give_money`) into the `TOOLS` creative tab. When a player uses these items on another player, it triggers a custom C2S packet (`MoneyPackets`). The server securely processes the packet, manipulating the defined vanilla scoreboard objective to transfer funds.

## Feature Breakdown
- **Interactive Economy Tools:** Introduces physical "Charge Money" and "Give Money" tools, allowing roleplay-friendly face-to-face transactions without typing commands.
- **Scoreboard Integration:** Seamlessly attaches to a standard Minecraft scoreboard objective (e.g., `money`) to read and manipulate balances.
- **Configurable Transaction Flow:** Server admins can configure whether transactions physically transfer money between the two involved players (`chargeToSelf`/`giveFromSelf` set to `true`) or if the items act as admin tools that generate/destroy currency from thin air (`false`).
- **Secure Client-Server Networking:** The actual financial math and scoreboard manipulation are securely handled server-side upon receiving the interaction payload, preventing client-side spoofing.

## Command Registry
*Note: This mod does not introduce any traditional chat commands. All functionality is handled via physical item interactions and their associated networking packets.*

## Configuration Schema
The mod expects/generates the configuration file at `config/payplayer.json`:

```json
{
  "scoreboardName": "money",
  "currencyName": "money",
  "currencySymbol": "$",
  "chargeToSelf": false,
  "giveFromSelf": false
}
```

## Developer Info
- **Author:** Me! (elthisboy)
- **Platform:** Fabric 1.21.1
