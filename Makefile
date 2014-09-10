MAKE_DISPLAY = true

RATS    = 100
PIPERS  = 1

SIZE    = 100

PIPER_PROGRAM = lessdumb
run:
	java piedpipers.sim.Piedpipers $(PIPER_PROGRAM) $(PIPERS) $(RATS) $(MAKE_DISPLAY) 6 $(SIZE)

headless:
	java piedpipers.sim.Piedpipers $(PIPER_PROGRAM) $(PIPERS) $(RATS) false 6 $(SIZE)
compile:
	javac ./piedpipers/sim/*.java
