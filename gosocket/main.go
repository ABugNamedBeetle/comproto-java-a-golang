package main

import (
	"crypto/tls"
	"crypto/x509"

	"strconv"
	"time"

	"encoding/pem"
	"fmt"
	"io"

	"log"
	"os"

	"golang.org/x/crypto/pkcs12"
)

func main() {

	p12Data, err := os.ReadFile("lkeystore.p12")
	if err != nil {
		log.Fatalf("error reading P12 file: %v", err)
	}

	// Replace "your_password" with the actual password
	blocks, err := pkcs12.ToPEM(p12Data, "1234")
	if err != nil {
		log.Fatalf("error decoding P12: %v", err)
	}

	var certPEM, keyPEM []byte
	for _, b := range blocks {
		// fmt.Println(string(b.Bytes))
		if b.Type == "CERTIFICATE" && len(b.Headers) != 0 { // intermediate and root will not have a header maps

			certPEM = pem.EncodeToMemory(b)
		} else if b.Type == "PRIVATE KEY" {
			keyPEM = pem.EncodeToMemory(b)
		}
	}

	cert, err := tls.X509KeyPair(certPEM, keyPEM)
	// cert, err := tls.LoadX509KeyPair("local.crt", "local-key.key")
	fmt.Println(cert)
	//trustore
	//Truststore
	caCert, err := os.ReadFile("inter.crt")
	if err != nil {
		log.Fatal(err)
	}

	caCertPool := x509.NewCertPool()
	caCertPool.AppendCertsFromPEM(caCert)

	if err != nil {
		log.Fatalf("server: loadkeys: %s", err)
	}
	config := tls.Config{InsecureSkipVerify: false, RootCAs: caCertPool, MinVersion: tls.VersionTLS12} // CipherSuites: []uint16{
	// 	// tls.TLS_AES_128_GCM_SHA256,
	// 	tls.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
	// 	tls.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
	// 	tls.TLS_RSA_WITH_AES_256_GCM_SHA384,
	// 	tls.TLS_RSA_WITH_AES_256_CBC_SHA,
	// },

	conn, err := tls.Dial("tcp", "redbear.local:5001", &config)
	if err != nil {
		log.Fatalf("client: dial: %s", err)
	}
	defer conn.Close()
	log.Println("client: connected to: ", conn.RemoteAddr())
	log.Println("jere")
	state := conn.ConnectionState()
	for _, v := range state.PeerCertificates {
		// fmt.Println(x509.MarshalPKIXPublicKey(v.PublicKey))
		fmt.Println(v.Subject)
	}
	log.Println("client: handshake: ", state.HandshakeComplete)
	// log.Println("client: mutual: ", state.NegotiatedProtocolIsMutual)

	// message := "Hello Arpit \n"
	// n, err := io.WriteString(conn, message)
	// if err != nil {
	// 	log.Fatalf("client: write: %s", err)
	// }
	// log.Printf("client: wrote %q (%d bytes)", message, n)

	// reply := make([]byte, 256)
	// n, err = conn.Read(reply)
	// log.Printf("client: read %q (%d bytes)", string(reply[:n]), n)
	// log.Print("client: exiting")

	//reader loop
	for i := range 1 {
		fmt.Println(i)
		go startSending(conn)
	}
	reply := make([]byte, 256)
	for {
		n, _ := conn.Read(reply)
		log.Printf("client: read %q (%d bytes)", string(reply[:n]), n)
	}

}

func startSending(conn *tls.Conn) {

	ticker := time.NewTicker(1 * time.Microsecond)
	defer ticker.Stop()

	count := 0
	for range ticker.C {
		fmt.Println("Sending Data")
		_, err := io.WriteString(conn, "Hello Data"+strconv.Itoa(count)+"\n")

		if err != nil {
			fmt.Println("Error writing to connection:", err)
			return
		}
		count++
		if count >= 10 {
			break
		}
	}
}
