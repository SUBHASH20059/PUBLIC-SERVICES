
CXX = g++
CXXFLAGS = -std=c++17 -O3 -Wall

TARGET = quantum_engine
SRC = quantum_engine.cpp

all: $(TARGET)

$(TARGET): $(SRC)
	$(CXX) $(CXXFLAGS) -o $(TARGET) $(SRC)

clean:
	rm -f $(TARGET)
