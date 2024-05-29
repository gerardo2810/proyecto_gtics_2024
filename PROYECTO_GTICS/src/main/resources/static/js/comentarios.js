const data = {
    currentUser: {
        image: {
            png: "https://via.placeholder.com/150",
            webp: "https://via.placeholder.com/150",
        },
        username: "Coronado-Farmacista",
    },
    comments: [
        {
            parent: 0,
            id: 1,
            content:
                "Impressive! Though it seems the drag feature could be improved. But overall it looks incredible. You've nailed the design and the responsiveness at various breakpoints works really well.",
            createdAt: "Hace una hora",
            user: {
                image: {
                    png: "https://via.placeholder.com/150",
                    webp: "https://via.placeholder.com/150",
                },
                username: "Araceli-SuperAdmin",
            },
            replies: [],
        },
    ],
};


function appendFrag(frag, parent) {
    var children = [].slice.call(frag.childNodes, 0);
    parent.appendChild(frag);
    return children[1];
}

const addComment = (body, parentId, replyTo = undefined) => {
    let commentParent =
        parentId === 0
            ? data.comments
            : data.comments.filter((c) => c.id == parentId)[0].replies;
    let newComment = {
        parent: parentId,
        id:
            commentParent.length == 0
                ? 1
                : commentParent[commentParent.length - 1].id + 1,
        content: body,
        createdAt: "Now",
        replyingTo: replyTo,
        replies: parentId == 0 ? [] : undefined,
        user: data.currentUser,
    };
    commentParent.push(newComment);
    initComments();
};

const deleteComment = (commentObject) => {
    if (commentObject.parent == 0) {
        data.comments = data.comments.filter((e) => e != commentObject);
    } else {
        let parentComment = data.comments.filter((e) => e.id === commentObject.parent)[0];
        parentComment.replies = parentComment.replies.filter((e) => e != commentObject);
    }
    initComments();
};

const promptDel = (commentObject) => {
    const modalWrp = document.querySelector(".modal-wrp");
    modalWrp.classList.remove("invisible");
    modalWrp.querySelector(".yes").addEventListener("click", () => {
        deleteComment(commentObject);
        modalWrp.classList.add("invisible");
    });
    modalWrp.querySelector(".no").addEventListener("click", () => {
        modalWrp.classList.add("invisible");
    });
};

const spawnReplyInput = (parent, parentId, replyTo = undefined) => {
    if (parent.querySelectorAll(".reply-input")) {
        parent.querySelectorAll(".reply-input").forEach((e) => {
            e.remove();
        });
    }
    const inputTemplate = document.querySelector(".reply-input-template");
    const inputNode = inputTemplate.content.cloneNode(true);
    const addedInput = appendFrag(inputNode, parent);
    addedInput.querySelector(".bu-primary").addEventListener("click", () => {
        let commentBody = addedInput.querySelector(".cmnt-input").value;
        if (commentBody.length == 0) return;
        addComment(commentBody, parentId, replyTo);
    });
};

const createCommentNode = (commentObject) => {
    const commentTemplate = document.querySelector(".comment-template");
    var commentNode = commentTemplate.content.cloneNode(true);
    commentNode.querySelector(".usr-name").textContent = commentObject.user.username;
    commentNode.querySelector(".usr-img").src = commentObject.user.image.webp;
    commentNode.querySelector(".cmnt-at").textContent = commentObject.createdAt;
    commentNode.querySelector(".c-body").textContent = commentObject.content;
    if (commentObject.replyingTo) {
        commentNode.querySelector(".reply-to").textContent = "@" + commentObject.replyingTo;
    }


    if (commentObject.user.username == data.currentUser.username) {
        commentNode.querySelector(".comment").classList.add("this-user");
        commentNode.querySelector(".delete").addEventListener("click", () => {
            promptDel(commentObject);
        });
        commentNode.querySelector(".edit").addEventListener("click", (e) => {
            const path = e.path[3].querySelector(".c-body");
            if (!path.getAttribute("contenteditable")) {
                path.setAttribute("contenteditable", true);
                path.focus();
            } else {
                path.removeAttribute("contenteditable");
            }
        });
        return commentNode;
    }

    return commentNode;
};

const appendComment = (parentNode, commentNode, parentId) => {
    const bu_reply = commentNode.querySelector(".reply");
    const appendedCmnt = appendFrag(commentNode, parentNode);
    const replyTo = appendedCmnt.querySelector(".usr-name").textContent;
    bu_reply.addEventListener("click", () => {
        if (parentNode.classList.contains("replies")) {
            spawnReplyInput(parentNode, parentId, replyTo);
        } else {
            spawnReplyInput(appendedCmnt.querySelector(".replies"), parentId, replyTo);
        }
    });
};

function filterComments(commentList) {
    return commentList.filter(comment => {
        const allowedUsers = ["Araceli-SuperAdmin", "Coronado-Farmacista"];
        if (!allowedUsers.includes(comment.user.username)) {
            return false;
        }
        if (comment.replies && comment.replies.length > 0) {
            comment.replies = filterComments(comment.replies);
        }
        return true;
    });
}

function initComments(
    commentList = data.comments,
    parent = document.querySelector(".comments-wrp")
) {
    parent.innerHTML = "";
    commentList = filterComments(commentList);
    commentList.forEach((element) => {
        var parentId = element.parent == 0 ? element.id : element.parent;
        const comment_node = createCommentNode(element);
        if (element.replies && element.replies.length > 0) {
            initComments(element.replies, comment_node.querySelector(".replies"));
        }
        appendComment(parent, comment_node, parentId);
    });
}

// Función para inicializar los comentarios
function initComments(commentList = data.comments, parent = document.querySelector(".comments-wrp")) {
    parent.innerHTML = ""; // Limpiar el contenido existente
    commentList.forEach((element) => {
        var parentId = element.parent == 0 ? element.id : element.parent;
        const comment_node = createCommentNode(element);
        if (element.replies && element.replies.length > 0) {
            initComments(element.replies, comment_node.querySelector(".replies"));
        }
        appendComment(parent, comment_node, parentId);
    });

    // Aplicar estilo a las imágenes de usuario en los comentarios
    const userImages = document.querySelectorAll(".usr-img");
    userImages.forEach((img) => {
        img.style.height = "65px";
        img.style.borderRadius = "50px";
        img.style.width = "auto";
    });
}

initComments();

const cmntInput = document.querySelector(".reply-input");
cmntInput.querySelector(".bu-primary").addEventListener("click", () => {
    let commentBody = cmntInput.querySelector(".cmnt-input").value;
    if (commentBody.length == 0) return;
    addComment(commentBody, 0);
    cmntInput.querySelector(".cmnt-input").value = "";
});
// Conexión WebSocket
const socket = new WebSocket('ws://tu-servidor-websocket.com');

// Evento de apertura de conexión WebSocket
socket.addEventListener('open', function (event) {
    console.log('Conexión WebSocket abierta');
});

// Evento de recepción de mensajes del servidor
socket.addEventListener('message', function (event) {
    const nuevoComentario = JSON.parse(event.data);
    // Actualizar la interfaz de usuario con el nuevo comentario recibido
    initComments(); // Vuelve a cargar todos los comentarios, incluido el nuevo
});

// Función para enviar comentarios al servidor
function enviarComentarioAlServidor(comentario) {
    socket.send(JSON.stringify(comentario));
}
