
# Burrito

Team Member: Ruibo Liu & Tian Xia

## Development Goals

1. Swift development based on appropriate __Modularity__, including needed Abstraction and MVC project structure.
2. High-Efficiency threads control and smart service progress lifecycle design. 
3. Smooth user experience and friendly UI.
4. Several available filter, based on algorithms specially designed. 
5. Social Network features, which enable users to share their edited images on social media!

## Highly-Abstract Architecture

![](https://s3.amazonaws.com/artceleration/Ass2.png)

Above is the project structure in high level. All activities are marked with circles and classes are marked with rounded rectangles. We could see that ArtLib is the core for the whole structure. Besides the helper classes and needed supported classes provided by professor, I created three other classes to implement service and thread. 

## FIFO & Separate Thread… How does it work? 

![](https://s3.amazonaws.com/artceleration/truck_whole.png)

Above is a big picture of the whole story. We assumes __the artTransformService__ to be a burrito making service. For convenience, we provide a truck for this service, such as we provide our __artTransformService__ with a __separate thread__. The process of making burrito is like a __runnable__ event, and instead of doing by ourselves, we hire a guy to handle all the burrito things, like receiving orders and making burritos. 

“Receiving orders” is just like “receiving message”. __Message__, in our app, is sent when the ArtLib needs the __artTransformService__ (with all the data). Similarly, in the burrito truck, the cook receives orders/messages from the customers who need this snack service. A better way to handle these trivial orders is to hire another guy who can help us loop through the order in a FIFO manner, as professor mentioned.

![](https://s3.amazonaws.com/artceleration/truck.png)

Now, we hire a girl to help us do the “Looper” job. Her name is “__Looper__”. What she does is: Every time she notices the cook is about to finish an order, she grabs the next order from the “__Message Queue__” and gives it to the cook. That’s basically a __FIFO manner__, because of the existing of message pool.

## Corresponding Code Strategy
### ArtTransformService
```java
	public class ArtTransformService extends Service{

    private static final String TAG = ArtTransformService.class.getSimpleName();
    
    public Messenger mMessenger = new Messenger(new ArtTransformHandler());
    private ArtTransformHandler mArtTransformHandler;
    public ArtTransformService() {
    }


    @Override
    public void onCreate() {

        // Put service into a separate Thread named "ArtTransformThread"
        ArtTransformThread thread = new ArtTransformThread();
        thread.setName("ArtTransformThread");
        thread.start();

        // Make sure Handler is available
        while (thread.mArtTransformHandler == null) {

        }
        mArtTransformHandler = thread.mArtTransformHandler;
        mArtTransformHandler.setService(this);
    }
    ...
```

In @Overriding OnCreat() Method, we initiate a separate thread for our artTransformService. The while() {} loop is needed because we should make sure out handler is available before we give the reference to the handler to the thread class.

### ArtTransformThread
```java
public class ArtTransformThread extends Thread{
    private static final String TAG = ArtTransformThread.class.getSimpleName();
    public ArtTransformHandler mArtTransformHandler;

    @Override
    public void run() {
        Looper.prepare();
        mArtTransformHandler = new ArtTransformHandler();
        Looper.loop();
    }
}
```

Nothing fancy. Actually there are two ways to create a separate thread. Besides above, we could also announce a new thread in the Manifest file.

### ArtTransformHandler
```java
public class ArtTransformHandler extends Handler implements TransformHandler{
    private ArtTransformService mService;
    private Bitmap img_out;

    @Override
    public void handleMessage(Message msg) {
        
        // Handle different types of transform requests based on "what"
        doTransform(msg.what);

        Log.d("option", String.valueOf(msg.what));

        Bundle bundle = msg.getData();
        ParcelFileDescriptor pfd = bundle.getParcelable("pfd");
        FileInputStream fios = new ParcelFileDescriptor.AutoCloseInputStream(pfd);
        img_out = BitmapFactory.decodeStream(fios);
     }
     ...
```
We implement TransformHandler Interface and do some simple message handle process here. More complicated Callback or Conditional function would be implemented when transform algorithms come in in the future.

## Challenge

In fact, there's nothing too challenging. Maybe the debug progress is something frustrating.

![](https://s3.amazonaws.com/artceleration/thread.png)

That is DDMS to make sure we manage to create a separate thread.

![](https://s3.amazonaws.com/artceleration/debug.png)

Above is debug log. Click different option could send different message to handler. After processing, the onTransformProcessed() Method would be triggered.

## Improvement/Potential Extra Credits 

I think we could do something more on the service connection management, because service also has its lifecycle. If we do not consider about when the service should connect, when disconnect, it would always stay in our users' background progresses and consume device resource all the time. 

So we have tried some mechanisms like:

```java
...
@Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Message message = Message.obtain();
        message.arg1 = startId;
        mArtTransformHandler.sendMessage(message);
        return Service.START_REDELIVER_INTENT;
    }
...
```

We assume value of "arg1" in message to be starId, which is a parameter recording the unique ID of service progress everytime the service is called.

```java
...
@Override
    public void handleMessage(Message msg) {
        
        ...

        // Stop the service progress based on unique ID 
        mService.stopSelf(msg.arg1);
    }
...
```

Above is in the Handler class. It enables out service progress could stop by itself after all its message queue has been handled yet. The keypoint is, if our user kill the service progress for some reasons, the service would automatically continue its undo queue handle job!

That's all. Really thanks professor. It’s a great experience.


