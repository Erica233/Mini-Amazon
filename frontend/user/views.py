from django.shortcuts import render, redirect
from .forms import UserRegisterForm
from django.contrib import messages

def register(request):
    if request.method == 'POST':
        user = UserRegisterForm(request.POST)
        if user.is_valid():
            user.save()
            username = user.cleaned_data.get('username')
            messages.success(request, f'Account successfully created for {username}!')
            return redirect('amazon-home')
    else:
        user = UserRegisterForm()
    return render(request, 'user/register.html', {'form': user})
