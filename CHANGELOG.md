## Version 1.1.0

### Added
- Dye removal for rope couplings: hold a water bucket and right-click a colored strand to bleach it back to its natural color, or craft a colored rope coupling with a water bucket to get a plain one back

### Changed
- Improved rope strand rendering performance by resolving tint color once per render call instead of once per segment

### Fixed
- Fixed a crash on dedicated servers caused by a client-only network handler being registered on the server
- Fixed off-hand dye not working when dyeing strands in-world
- Fixed strand colors persisting across world sessions on the client, which could cause strands to appear incorrectly colored after reconnecting
- Fixed the server accepting dye requests for strands outside the player's reach
- Fixed the server accepting invalid color values from malformed network packets
