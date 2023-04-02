package zad1;

import java.nio.Buffer;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

class Buffers {

    static void say(String s) { System.out.println(s); }

    static void showParms(String msg, Buffer b) {
        say("Charakterystyki bufora - " + msg);
        say("capacity  :" + b.capacity());
        say("limit     :" + b.limit());
        say("position  :" + b.position());
        say("remaining :" + b.remaining());
    }

    public static void main(String args[]) {

        ByteBuffer b = ByteBuffer.allocate(10);

        showParms("Po utworzeniu", b);

        b.put((byte) 7).put((byte) 9);
        showParms("Po dodaniu dwóch elementów", b);

        b.flip();
        showParms("Po przestawieniu", b);

        say("Czytamy pierwszy element: " + b.get());
        showParms("Po pobraniu pierwszego elementu", b);
        say("Czytamy drugi element: " + b.get());
        showParms("Po pobraniu drugiego elementu", b);

        say("Czy możemy jeszcze czytać?");
        try {
            byte x = b.get();
        } catch (BufferUnderflowException exc) {
            say("No, nie - proszę spojrzeć na ostatni limit!");
        }

        b.rewind();
        showParms("Po przewinięciu", b);

        say("Czytanie wszystkiego, co wpisaliśmy");
        while (b.hasRemaining())
            say("Jest: " + b.get());
    }
}
