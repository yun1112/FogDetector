package com.leadstepapp.bluetoothseriallibrary

import android.bluetooth.BluetoothSocket
import android.text.TextUtils
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Implementation of BluetoothSerialDevice, package-private
 */
internal class BluetoothSerialDeviceImpl constructor(
        override val mac: String,
        private val socket: BluetoothSocket,
        private val charset: Charset
) : BluetoothSerialDevice {
    private val closed = AtomicBoolean(false)
    override val outputStream: OutputStream = socket.outputStream
    override val inputStream: InputStream = socket.inputStream

    private var owner: SimpleBluetoothDeviceInterfaceImpl? = null

    override fun send(message: String): Completable {
        checkNotClosed()
        return Completable.fromAction {
            synchronized(outputStream) {
                if (!closed.get()) outputStream.write(Integer.parseInt(message))
            }
        }
    }

    override fun startInsole(): Completable {
        checkNotClosed()
        return Completable.fromAction {
            synchronized(outputStream) {
                if (!closed.get())
                    outputStream.write(0x00);
                    outputStream.write(0xA2);
                    outputStream.write(0x00);
                    outputStream.write(0xFF);
            }
        }
    }
    override fun stopInsole(): Completable {
        checkNotClosed()
        return Completable.fromAction {
            synchronized(outputStream) {
                if (!closed.get())
                    outputStream.write(0x00);
                outputStream.write(0xA0);
                outputStream.write(0x00);
                outputStream.write(0xFF);
            }
        }
    }

    override fun myInputStream():Flowable<String>{
        return Flowable.create({ emitter ->
            var arr : String
            while (!emitter.isCancelled && !closed.get()) {
                synchronized(inputStream) {
                    arr = inputStream.read().toString()
                    emitter.onNext(arr)
                }
            }
            emitter.onComplete()
        }, BackpressureStrategy.BUFFER)
        }

    override fun openMessageStream(): Flowable<String> {
        checkNotClosed()
        return Flowable.create({ emitter ->
            val reader = BufferedInputStream(inputStream)
            while (!emitter.isCancelled && !closed.get()) {
                synchronized(inputStream) {
                    try {


                        emitter.onNext(reader.read().toString())

                    } catch (e: Exception) {
                        if (!emitter.isCancelled && !closed.get()) {
                            emitter.onError(e)
                        }
                    }
                }
            }
            emitter.onComplete()
        }, BackpressureStrategy.BUFFER)
    }

    fun close() {
        if (!closed.get()) {
            closed.set(true)
            inputStream.close()
            outputStream.close()
            socket.close()
        }
        owner?.close()
        owner = null
    }

    override fun toSimpleDeviceInterface(): SimpleBluetoothDeviceInterfaceImpl {
        checkNotClosed()
        owner?.let { return it }
        val newOwner = SimpleBluetoothDeviceInterfaceImpl(this)
        owner = newOwner
        return newOwner
    }

    /**
     * Checks that this instance has not been closed
     */
    fun checkNotClosed() {
        check(!closed.get()) { "Device connection closed" }
    }
}
