# 6502-Processor-Emu
Un emulador de el famoso procesador 6502 implementado completamente en Java, con soporte para todas las instrucciones.
El proyecto solo incluye un procesador, no un ensamblador, junto con algunas clases auxiliares para hacer mas facil su
extension, por ejemplo mostrar una pantalla y establecer el byte aleatorio. Este tipo de procesador no soporta conectar
otros dispositivos mediante puertos especiales, en cambio de deben hacer "Memory Mappings" para manejar cualquier
componente complementario (como la pantalla).
# Como utilizarlo ?
```java
...
//Creamos una instancia
var proc = CPU.newInstance();

//Configuramos el procesador
proc.setup(
	true, //Clear Memory
	true, //Clear Flags
	true, //Clear Registers
	0x600 //Program Counter
);

//Cargamos la memoria
cpu.memory().load(
	... /*File or bytes*/,
	12340, //Data Length
	
);

//Configuramos el Byte Aleatorio
//Opcional
cpu.addPreInstruction(new SetRandom());

//Ejecutamos
//NOTA:
//Podrias utilizar el metodo "executeAync(Runnable)"
//pero personalmente no lo recomiendo

while(cpu.step()){
	...
}
...
```
# Licencia
Bajo la licencia Apache License 2.0 , para mas informacion leer el archivo LICENSE