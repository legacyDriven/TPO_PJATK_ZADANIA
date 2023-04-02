package zad1;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

class SimpleChannel {

    String fname = "test.tmp";
    byte[] data = {1,2,3,4,5 };

    SimpleChannel() {
        try {
            writeChannel(fname, data);
            byte[] wynik = readChannel(fname);
            for (int i=0; i < wynik.length; i++) System.out.println(wynik[i]);
        } catch(Exception exc) {
            exc.printStackTrace();
            System.exit(1);
        }
    }

    void writeChannel(String fname, byte[] data) throws Exception {

        // Możemy utworzyć bufor przez opakowanie istniejącej tablicy
        ByteBuffer buf = ByteBuffer.wrap(data);

        FileOutputStream out = new FileOutputStream(fname);

        // Uzyakanie kanału
        FileChannel fc = out.getChannel();

        //Zapis
        fc.write(buf);
        fc.close();
    }

    byte[] readChannel(String fname) throws Exception {

        // Używamy obiektu klasy File
        // by dowiedzieć się jaki jest rozmiar pliku
        // i odpowiednio alokowac bufor
        File file = new File(fname);

        // Stworzenie strumienia na podstawie obiektu klasy File
        FileInputStream in = new FileInputStream(file);

        // Uzyskanie kanału
        FileChannel fc = in.getChannel();

        // Metoda size() z klasy FileChannel
        // zwraca long -rozmiar plku, do którego podlączony jest kanał
        int size = (int) fc.size();

        // Utworzenie bufora
        ByteBuffer buf = ByteBuffer.allocate(size);

        // Czytanie do bufora
        // nbytes - liczba przeczytanych bajtów
        int nbytes = fc.read(buf);
        fc.close();

        // Po przeczytaniu danych musimy bufor przestawić
        buf.flip();

        // Stworzenie tablicy na wynik czytania
        // jej rozmiar będzie określony przez liczbę przeczytanuych bajtów
        // którą możemy podać na dwa sposoby: poprzednie nbytes
        // lub uzyskując informację o liczbie jeszcze nieodczytanych bajtów z bufora

        byte[] wynik = new byte[buf.remaining()];
        buf.get(wynik);
        return wynik;
    }

    public static void main(String args[]) {
        new SimpleChannel();
    }
}
