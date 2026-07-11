# Quantum Security Engine

This module provides a Quantum-Safe Security Engine implemented in C++ and exposed via a Python backend using the **Quater** framework.

## Features

- **C++ Core**: High-performance quantum-resistant cryptographic logic (PQC).
- **Quater Framework**: Unified exposure of security actions across HTTP, MCP, and CLI.
- **Quantum-Safe Algorithms**: Inspired by NIST PQC finalists like Kyber (KEM) and Dilithium (Digital Signatures).

## Structure

- `quantum_engine.cpp`: C++ source for the security engine.
- `Makefile`: Build instructions for the C++ engine.
- `main.py`: Quater-based Python wrapper exposing the engine.
- `quantum_engine`: Compiled binary of the security engine.

## Usage

### Build the Engine
```bash
make
```

### Run the Quater Service
```bash
quater main.py
```

### CLI Actions
You can trigger quantum security actions directly from the CLI:
```bash
quater-cli initialize_quantum_engine
```

## Security Model
The engine is protected by Quater's surface-wide authentication. Accessing the security tools via HTTP or MCP requires a valid `AuthContext`.
