package com.leadstepapp.bluetoothseriallibrary

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * Implementation of SimpleBluetoothDeviceInterface, package-private
 */
internal class SimpleBluetoothDeviceInterfaceImpl(override val device: BluetoothSerialDeviceImpl) : SimpleBluetoothDeviceInterface {
    private val compositeDisposable = CompositeDisposable()

    private var messageReceivedListener: SimpleBluetoothDeviceInterface.OnMessageReceivedListener? = null
    private var messageSentListener: SimpleBluetoothDeviceInterface.OnMessageSentListener? = null
    private var errorListener: SimpleBluetoothDeviceInterface.OnErrorListener? = null

    init {
        compositeDisposable.add(device.myInputStream()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ messageReceivedListener?.onMessageReceived(it) }, { errorListener?.onError(it) }))
    }

    override fun sendMessage(message: String) {
        device.checkNotClosed()
        compositeDisposable.add(device.send(message)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ messageSentListener?.onMessageSent(message) }, { errorListener?.onError(it) }))
    }
    override fun startInsole() {
        device.checkNotClosed()
        compositeDisposable.add(device.startInsole()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ messageSentListener?.onMessageSent("start insole command") }, { errorListener?.onError(it) }))
    }
    override fun stopInsole() {
        device.checkNotClosed()
        compositeDisposable.add(device.stopInsole()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ messageSentListener?.onMessageSent("stop insole command") }, { errorListener?.onError(it) }))
    }
    override fun setListeners(messageReceivedListener: SimpleBluetoothDeviceInterface.OnMessageReceivedListener?,
                              messageSentListener: SimpleBluetoothDeviceInterface.OnMessageSentListener?,
                              errorListener: SimpleBluetoothDeviceInterface.OnErrorListener?) {
        this.messageReceivedListener = messageReceivedListener
        this.messageSentListener = messageSentListener
        this.errorListener = errorListener
    }

    override fun setMessageReceivedListener(listener: SimpleBluetoothDeviceInterface.OnMessageReceivedListener?) {
        messageReceivedListener = listener
    }

    override fun setMessageSentListener(listener: SimpleBluetoothDeviceInterface.OnMessageSentListener?) {
        messageSentListener = listener
    }

    override fun setErrorListener(listener: SimpleBluetoothDeviceInterface.OnErrorListener?) {
        errorListener = listener
    }

    fun close() {
        compositeDisposable.dispose()
    }
}
