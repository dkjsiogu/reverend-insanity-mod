# AGENT.md

## Execution Contract

This file captures the current working contract for autonomous refactoring.

### User Requirements

- Work autonomously by priority.
- Complete one priority item at a time.
- Review the changed code before each commit.
- Rebuild after each completed item.
- Commit each completed item as its own batch.

### Current Priorities

1. Remove full-world entity scans for flash-blind expiration and replace with indexed tracking.
2. Reduce server-side event/network god-class pressure by extracting focused handlers.
3. Replace string-packed sync payload fields with structured payload data.
4. Improve lifecycle cleanup for static combat managers and ephemeral runtime caches.
5. Add minimal automated verification coverage for core combat/network contracts.
