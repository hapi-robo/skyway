const callForm = document.querySelector('#call-form');

let dataConnection;
let localMediaStream;

const initMediaConnection = (remoteId) => {
  const remoteVideo = document.getElementById('remote-video');
  const mediaConnection = peer.call(remoteId, localMediaStream);

  // play remote video stream
  mediaConnection.on('stream', (stream) => {
    console.log('Media connection: open');
    remoteVideo.srcObject = stream;
    remoteVideo.controls = true;
    remoteVideo.play();
  });
  mediaConnection.on('close', () => {
    console.log('Media connection: close');
    remoteVideo.srcObject = null;
  });
}

const initDataConnection = (remoteId) => {
  dataConnection = peer.connect(remoteId);
  dataConnection.on('open', () => { console.log('Data connection: open'); });
  dataConnection.on('close', () => { console.log('Data connection: close'); });
  dataConnection.on('data', (data) => { console.log(`Received data: ${data}`); });
};

callForm.addEventListener('submit', (e) => {
  e.preventDefault();
  const remoteId = callForm.remoteId.value;
  console.log(`Remote ID: ${remoteId}`);

  initMediaConnection(remoteId);
  initDataConnection(remoteId);
  
  callForm.reset();
});

function init() {
  callForm.reset();

  // capture local media
  navigator.mediaDevices.getUserMedia({ video: true, audio: true })
    .then(stream => {
      // play local video stream
      const localVideo = document.querySelector('#local-video');
      localVideo.srcObject = stream;
      localVideo.play();
      localMediaStream = stream;
    })
    .catch(err => {
      console.error('mediaDevice.getUserMedia() error: ', err);
    });
}

document.addEventListener('keydown', (e) => {
  // e.preventDefault();
  
  if (dataConnection === undefined) {
    return;
  }

  // https://keycode.info/
  console.log(e.code);
  dataConnection.send(e.code);
});

// event listeners
window.onload = init();
