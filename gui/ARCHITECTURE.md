# Log Level Filtering Architecture

## System Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          Python GUI (Client)                             │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │  Console Output Frame                                              │ │
│  │                                                                     │ │
│  │  Minimum Log Level: [DEBUG ▼]  ← User selects level               │ │
│  │                                                                     │ │
│  │  ┌──────────────────────────────────────────────────────────────┐ │ │
│  │  │ [01:18:30] Connected to robot                        (green)  │ │ │
│  │  │ [01:18:31] [ROBOT] Main: Application running         (black)  │ │ │
│  │  │ [01:18:31] [ROBOT] DEBUG: Handler initialized        (gray)   │ │ │
│  │  │ [01:18:32] [ROBOT] WARN: High memory usage          (orange)  │ │ │
│  │  │ [01:18:33] [ROBOT] ERROR: Connection lost             (red)   │ │ │
│  │  └──────────────────────────────────────────────────────────────┘ │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                           │
│  Filtering Logic:                                                        │
│  • log_console() checks message level vs selected level                 │
│  • Only displays messages at or above selected level                    │
│  • Color codes based on severity                                        │
│                                                                           │
└─────────────────────────────────────────────────────────────────────────┘
                                      │
                                      │ TCP/IP (JSON)
                                      │ set_log_level command
                                      ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                     KUKA Robot Controller (Server)                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ ConsoleCommandHandler (per client)                              │   │
│  │                                                                  │   │
│  │  • Receives set_log_level command                               │   │
│  │  • Creates NetworkListener with PrintWriter                     │   │
│  │  • Calls networkListener.setMinimumLevel(level)                 │   │
│  │  • Registers listener with LogManager                           │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                            │                                             │
│                            │ registers                                   │
│                            ▼                                             │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ LogManager (singleton)                                           │   │
│  │                                                                  │   │
│  │  • Maintains list of ILogListener implementations               │   │
│  │  • Broadcasts LogEntry to all registered listeners              │   │
│  │  • Thread-safe using CopyOnWriteArrayList                       │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                            │                                             │
│                            │ broadcasts to                               │
│                            ▼                                             │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ NetworkListener (per client)                                     │   │
│  │                                                                  │   │
│  │  private volatile LogLevel _minLevel = LogLevel.DEBUG;          │   │
│  │                                                                  │   │
│  │  public void onNewLog(LogEntry entry) {                         │   │
│  │      if (entry.getLevel().ordinal() >= _minLevel.ordinal()) {   │   │
│  │          _out.println(entry);  // Send to client                │   │
│  │      }                                                           │   │
│  │  }                                                               │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                            │                                             │
│                            │ filtered logs                               │
│                            ▼                                             │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ PrintWriter → TCP Socket → Client                               │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                           │
└─────────────────────────────────────────────────────────────────────────┘
```

## Log Level Hierarchy

```
DEBUG (0) ──┐
            ├─→ Most verbose
INFO  (1) ──┤
            │
WARN  (2) ──┤
            ├─→ Least verbose
ERROR (3) ──┘
```

### Filtering Rule:
```
message.level.ordinal() >= minLevel.ordinal()
```

### Examples:

**When minLevel = INFO:**
- DEBUG messages: `0 >= 1` → ❌ Filtered out
- INFO messages:  `1 >= 1` → ✅ Displayed
- WARN messages:  `2 >= 1` → ✅ Displayed
- ERROR messages: `3 >= 1` → ✅ Displayed

**When minLevel = ERROR:**
- DEBUG messages: `0 >= 3` → ❌ Filtered out
- INFO messages:  `1 >= 3` → ❌ Filtered out
- WARN messages:  `2 >= 3` → ❌ Filtered out
- ERROR messages: `3 >= 3` → ✅ Displayed

## Message Flow

```
1. Application Code
   │
   │ Logger.info("Message")
   ▼
2. Logger
   │
   │ Creates LogEntry
   ▼
3. LogManager.broadcast(entry)
   │
   │ Distributes to all listeners
   ├────────────────┬───────────────┬────────────────┐
   ▼                ▼               ▼                ▼
4. LogCollector  NetworkListener  NetworkListener  ...
                (Client 1)       (Client 2)
   │                │               │
   │                │ Filters       │ Filters
   │                ▼               ▼
5. LogPublisher  TCP Socket 1    TCP Socket 2
   │
   │ System.out
   ▼
   Console
```

## Client-Server Interaction Sequence

```
Client                                    Server
  │                                         │
  │ ── Connect to port 30001 ─────────────→ │
  │                                         │
  │ ←─── "Connected" message ──────────────│
  │                                         │
  │                                         │ (NetworkListener created
  │                                         │  with minLevel = DEBUG)
  │                                         │
  │ ── {"type":"set_log_level",  ─────────→│
  │     "level":"INFO"}                     │
  │                                         │
  │                                         │ networkListener.setMinimumLevel(INFO)
  │                                         │
  │ ←─── {"type":"response", ──────────────│
  │       "message":"Log level set to INFO",│
  │       "success":true}                   │
  │                                         │
  │                                         │ Application logs:
  │                                         │   Logger.debug("Details")  ─┐
  │                                         │   Logger.info("Started")   ─┤
  │                                         │   Logger.warn("Warning")   ─┤
  │                                         │                             │
  │                                         │ NetworkListener filters:   │
  │                                         │   DEBUG → blocked           │
  │                                         │   INFO → sent               │
  │                                         │   WARN → sent               │
  │                                         │                             │
  │ ←─── [13:45:23.456] Main | INFO... ────┤
  │ ←─── [13:45:24.123] Main | WARN... ────┘
  │                                         │
  │ (GUI also filters based on its          │
  │  own log level selection)               │
  │                                         │
```

## Benefits of Dual Filtering

### Server-Side Filtering (NetworkListener)
✅ Reduces network bandwidth
✅ Reduces server I/O overhead
✅ Independent filter per client
✅ No buffering needed

### Client-Side Filtering (GUI)
✅ Immediate UI response when changing level
✅ User can change view without affecting server
✅ Additional safety layer
✅ Better user experience

## Thread Safety

The implementation is thread-safe:

1. **LogManager**: Uses `CopyOnWriteArrayList` for listeners
2. **NetworkListener**: `volatile LogLevel _minLevel` for thread visibility
3. **ConsoleCommandHandler**: Proper cleanup in finally block
4. **No shared mutable state** between clients

## Java 1.7 Compatibility

All code adheres to Java 1.7 constraints:
- ✅ No lambdas
- ✅ No streams
- ✅ No diamond operators
- ✅ Traditional for loops
- ✅ Explicit generic types
- ✅ `volatile` for thread safety instead of `AtomicReference`
